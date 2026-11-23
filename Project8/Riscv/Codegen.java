/**
 * @authors Luke Sam
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
package Riscv;

import java.util.LinkedList;
import java.util.List;
import Assem.Instr;
import Assem.OPER;
import Temp.Label;
import Temp.Temp;
import Tree.*;

public class Codegen implements CodeVisitor
{
    private final RiscvFrame frame;
    private final LinkedList<Instr> ilist = new LinkedList<Instr>();

    public Codegen(RiscvFrame f) { frame = f; }

    public List<Instr> instructions() { return ilist; }

    /* helpers */

    private void emit(Instr i) { ilist.add(i); }

    private static Temp[] L(Temp... t) { return t; }

    private static List<Label> labels(Label... ls)
    {
        LinkedList<Label> l = new LinkedList<Label>();
        for (Label x : ls) l.add(x);
        return l;
    }

    private static boolean const12(Exp e)
    {
        if (!(e instanceof CONST)) return false;
        int v = ((CONST) e).value;
        return v >= -2048 && v <= 2047;
    }

    private static int val(Exp e) { return ((CONST) e).value; }

    private static int log2(Exp e)
    {
        if (!(e instanceof CONST)) return -1;
        int v = val(e);
        if (v < 2 || (v & (v - 1)) != 0) return -1;
        return Integer.numberOfTrailingZeros(v);
    }

    private Temp rrr(String op, Temp a, Temp b)
    {
        Temp d = new Temp();
        emit(new OPER(op + " `d0, `s0, `s1", L(d), L(a, b), null));
        return d;
    }

    private Temp rri(String op, Temp a, int imm)
    {
        Temp d = new Temp();
        emit(new OPER(op + " `d0, `s0, " + imm, L(d), L(a), null));
        return d;
    }

    /* statements */

    public void visit(SEQ n)
    {
        n.left.accept(this);
        n.right.accept(this);
    }

    public void visit(LABEL n)
    {
        emit(new Assem.LABEL(n.label + ":", n.label));
    }

    public void visit(JUMP n)
    {
        if (n.exp instanceof NAME)
        {
            Label target = ((NAME) n.exp).label;
            emit(new OPER("j `j0", null, null, labels(target)));
        }
        else
        {
            Temp rs = n.exp.accept(this);
            emit(new OPER("jalr `s0", null, L(rs),
                          n.targets == null ? new LinkedList<Label>()
                                            : new LinkedList<Label>(n.targets)));
        }
    }

    private static String branch(int relop)
    {
        switch (relop)
        {
        case CJUMP.EQ:  return "beq";
        case CJUMP.NE:  return "bne";
        case CJUMP.LT:  return "blt";
        case CJUMP.GT:  return "bgt";
        case CJUMP.LE:  return "ble";
        case CJUMP.GE:  return "bge";
        case CJUMP.ULT: return "bltu";
        case CJUMP.ULE: return "bleu";
        case CJUMP.UGT: return "bgtu";
        case CJUMP.UGE: return "bgeu";
        default: throw new Error("Codegen: bad relop " + relop);
        }
    }

    public void visit(CJUMP n)
    {
        Temp l = n.left.accept(this);
        Temp r = n.right.accept(this);

        emit(new OPER(branch(n.relop) + " `s0, `s1, `j0", null, L(l, r),
                      labels(n.iftrue, n.iffalse)));
    }


    public void visit(MOVE n)
    {
        if (n.dst instanceof MEM)
        {
            Exp addr = ((MEM) n.dst).exp;
            Temp base;
            int off = 0;
            if (addr instanceof BINOP && ((BINOP) addr).binop == BINOP.PLUS
                && const12(((BINOP) addr).right))
            {
                base = ((BINOP) addr).left.accept(this);
                off = val(((BINOP) addr).right);
            }
            else if (addr instanceof BINOP && ((BINOP) addr).binop == BINOP.PLUS
                     && const12(((BINOP) addr).left))
            {
                base = ((BINOP) addr).right.accept(this);
                off = val(((BINOP) addr).left);
            }
            else
                base = addr.accept(this);
            Temp src = n.src.accept(this);
            emit(new OPER("sw `s0, " + off + "(`s1)", null, L(src, base), null));
        }
        else if (n.dst instanceof TEMP)
        {
            Temp d = ((TEMP) n.dst).temp;
            Temp s = n.src.accept(this);
            emit(new Assem.MOVE("mv `d0, `s0", d, s));
        }
        else
            throw new Error("Codegen: MOVE destination must be TEMP or MEM");
    }

public void visit(EXP n)
    {
        n.exp.accept(this);
    }

    /* expressions */

    public Temp visit(TEMP n) { return n.temp; }

    public Temp visit(CONST n)
    {
        if (n.value == 0) return RiscvFrame.zero();
        Temp d = new Temp();
        emit(new OPER("li `d0, " + n.value, L(d), null, null));
        return d;
    }

    public Temp visit(NAME n)
    {
        Temp d = new Temp();
        emit(new OPER("la `d0, " + n.label, L(d), null, null));
        return d;
    }

    public Temp visit(ESEQ n)
    {
        throw new Error("Codegen: ESEQ should have been removed by Canon");
    }

    public Temp visit(MEM n)
    {
        Exp a = n.exp;
        if (a instanceof BINOP && ((BINOP) a).binop == BINOP.PLUS)
        {
            BINOP p = (BINOP) a;
            if (const12(p.left))
            {
                Temp rs = p.right.accept(this);
                return load(rs, val(p.left));
            }
            if (const12(p.right))
            {
                Temp rs = p.left.accept(this);
                return load(rs, val(p.right));
            }
        }
        return load(a.accept(this), 0);
    }

    private Temp load(Temp base, int off)
    {
        Temp d = new Temp();
        emit(new OPER("lw `d0, " + off + "(`s0)", L(d), L(base), null));
        return d;
    }

    public Temp visit(BINOP n)
    {
        Exp l = n.left, r = n.right;
        switch (n.binop)
        {
        case BINOP.PLUS:
            if (const12(r)) return rri("addi", l.accept(this), val(r));
            if (const12(l)) return rri("addi", r.accept(this), val(l));
            return rrr("add", l.accept(this), r.accept(this));

        case BINOP.MINUS:
            if (const12(r) && val(r) != -2048)
                return rri("addi", l.accept(this), -val(r));
            return rrr("sub", l.accept(this), r.accept(this));

        case BINOP.MUL:
        {
            int k;
            if ((k = log2(r)) > 0) return rri("sll", l.accept(this), k);
            if ((k = log2(l)) > 0) return rri("sll", r.accept(this), k);
            return rrr("mul", l.accept(this), r.accept(this));
        }

        case BINOP.DIV:
        {
            int k = log2(r);
            if (k > 0) return rri("sra", l.accept(this), k);
            return rrr("div", l.accept(this), r.accept(this));
        }

        case BINOP.AND:
        case BINOP.BITAND:
            if (const12(r)) return rri("andi", l.accept(this), val(r));
            if (const12(l)) return rri("andi", r.accept(this), val(l));
            return rrr("and", l.accept(this), r.accept(this));

        case BINOP.OR:
        case BINOP.BITOR:
            if (const12(r)) return rri("ori", l.accept(this), val(r));
            if (const12(l)) return rri("ori", r.accept(this), val(l));
            return rrr("or", l.accept(this), r.accept(this));

        case BINOP.BITXOR:
            if (const12(r)) return rri("xori", l.accept(this), val(r));
            if (const12(l)) return rri("xori", r.accept(this), val(l));
            return rrr("xor", l.accept(this), r.accept(this));

        case BINOP.LSHIFT:
            return shift("sll", l, r);
        case BINOP.RSHIFT:
            return shift("srl", l, r);
        case BINOP.ARSHIFT:
            return shift("sra", l, r);

        default:
            throw new Error("Codegen: unknown BINOP " + n.binop);
        }
    }

    private Temp shift(String op, Exp l, Exp r)
    {
        if (const12(r)) return rri(op, l.accept(this), val(r));
        return rrr(op, l.accept(this), r.accept(this));
    }

    public Temp visit(CALL n)
    {
        Temp fn = null;
        if (!(n.func instanceof NAME)) fn = n.func.accept(this);

        Temp[] argRegs = RiscvFrame.argRegs();
        LinkedList<Temp> uses = new LinkedList<Temp>();
        if (fn != null) uses.add(fn);

        int i = 0;
        for (Exp arg : n.args)
        {
            Temp t = arg.accept(this);
            if (i < argRegs.length)
            {
                emit(new Assem.MOVE("mv `d0, `s0", argRegs[i], t));
                uses.add(argRegs[i]);
            }
            else
            {
                int off = (i - argRegs.length) * frame.wordSize();
                emit(new OPER("sw `s0, " + off + "(`s1)", null,
                              L(t, RiscvFrame.sp()), null));
                frame.maxArgOffset = Math.max(frame.maxArgOffset,
                                              off + frame.wordSize());
            }
            i++;
        }

        Temp[] use = uses.toArray(new Temp[0]);
        if (n.func instanceof NAME)
            emit(new OPER("call " + ((NAME) n.func).label,
                          RiscvFrame.calldefs(), use, null));
        else
            emit(new OPER("jalr `s0", RiscvFrame.calldefs(), use, null));
        return frame.RV();
    }
}
