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

    /*  helpers  */

    private void emit(Instr i) { ilist.add(i); }

    private static Temp[] L(Temp... t) { return t; }

    private static List<Label> labels(Label... ls)
    {
        LinkedList<Label> l = new LinkedList<Label>();
        for (Label x : ls) l.add(x);
        return l;
    }

    private static String fmt(String op, String... args)
    {
        StringBuilder sb = new StringBuilder("\t" + op);
        for (int i = 0; i < args.length; i++)
            sb.append(i == 0 ? "\t" : ",\t").append(args[i]);
        return sb.toString();
    }

    private static boolean isConst(Exp e, int v)
    {
        return e instanceof CONST && ((CONST) e).value == v;
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

    private boolean isFP(Exp e)
    {
        return e instanceof TEMP && ((TEMP) e).temp == frame.FP();
    }

    private String framesize() { return frame.name + "_framesize"; }

    private Temp rrr(String op, Exp a, Exp b)
    {
        Temp d = new Temp();
        Temp ta = a.accept(this);
        Temp tb = b.accept(this);
        emit(new OPER(fmt(op, "`d0", "`s0", "`s1"), L(d), L(ta, tb), null));
        return d;
    }

    private Temp rri(String op, Exp a, int imm)
    {
        Temp d = new Temp();
        Temp ta = a.accept(this);
        emit(new OPER(fmt(op, "`d0", "`s0", "" + imm), L(d), L(ta), null));
        return d;
    }

    /*  statements  */

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
            emit(new OPER(fmt("j", "`j0"), null, null, labels(target)));
        }
        else
        {
            Temp rs = n.exp.accept(this);
            emit(new OPER(fmt("jalr", "`s0"), null, L(rs),
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
        emit(new OPER(fmt(branch(n.relop), "`s0", "`s1", "`j0"), null, L(l, r),
                      labels(n.iftrue, n.iffalse)));
    }

    public void visit(MOVE n)
    {
        if (n.dst instanceof MEM)
        {
            Exp addr = ((MEM) n.dst).exp;

            if (addr instanceof BINOP && ((BINOP) addr).binop == BINOP.PLUS)
            {
                BINOP p = (BINOP) addr;
                Exp base = null; Exp k = null;
                if (const12(p.right))     { base = p.left;  k = p.right; }
                else if (const12(p.left)) { base = p.right; k = p.left;  }

                if (base != null && isFP(base))          // MEM(fp + k)
                {
                    Temp src = n.src.accept(this);
                    emit(new OPER(fmt("sw", "`s0", val(k) + "+" + framesize() + "(`s1)"),
                                  null, L(src, RiscvFrame.sp()), null));
                    return;
                }
                if (base != null)                        // MEM(e + k)
                {
                    Temp b = base.accept(this);
                    Temp src = n.src.accept(this);
                    emit(new OPER(fmt("sw", "`s0", val(k) + "(`s1)"),
                                  null, L(src, b), null));
                    return;
                }
            }
            Temp b = addr.accept(this);                  // MEM(e)
            Temp src = n.src.accept(this);
            emit(new OPER(fmt("sw", "`s0", "(`s1)"), null, L(src, b), null));
        }
        else if (n.dst instanceof TEMP)
        {
            Temp d = ((TEMP) n.dst).temp;
            Temp s = n.src.accept(this);
            emit(new Assem.MOVE(fmt("mv", "`d0", "`s0"), d, s));
        }
        else
            throw new Error("Codegen: MOVE destination must be TEMP or MEM");
    }

    public void visit(EXP n)
    {
        n.exp.accept(this);
    }

    /*  expressions  */

    public Temp visit(TEMP n)
    {
        if (n.temp == frame.FP())                        // virtual fp
        {
            Temp d = new Temp();
            emit(new OPER(fmt("addi", "`d0", "`s0", framesize()),
                          L(d), L(RiscvFrame.sp()), null));
            return d;
        }
        return n.temp;
    }

    public Temp visit(CONST n)
    {
        if (n.value == 0) return RiscvFrame.zero();
        Temp d = new Temp();
        emit(new OPER(fmt("li", "`d0", "" + n.value), L(d), null, null));
        return d;
    }

    public Temp visit(NAME n)
    {
        Temp d = new Temp();
        emit(new OPER(fmt("la", "`d0", n.label.toString()), L(d), null, null));
        return d;
    }

    public Temp visit(ESEQ n)
    {
        throw new Error("Codegen: ESEQ should have been removed by Canon");
    }

    public Temp visit(MEM n)
    {
        Exp a = n.exp;
        Temp d = new Temp();                             // result first

        if (a instanceof BINOP && ((BINOP) a).binop == BINOP.PLUS)
        {
            BINOP p = (BINOP) a;
            Exp base = null; Exp k = null;
            if (const12(p.right))     { base = p.left;  k = p.right; }
            else if (const12(p.left)) { base = p.right; k = p.left;  }

            if (base != null && isFP(base))              // MEM(fp + k)
            {
                emit(new OPER(fmt("lw", "`d0", val(k) + "+" + framesize() + "(`s0)"),
                              L(d), L(RiscvFrame.sp()), null));
                return d;
            }
            if (base != null)                            // MEM(e + k)
            {
                Temp b = base.accept(this);
                emit(new OPER(fmt("lw", "`d0", val(k) + "(`s0)"), L(d), L(b), null));
                return d;
            }
        }
        Temp b = a.accept(this);                         // MEM(e)
        emit(new OPER(fmt("lw", "`d0", "(`s0)"), L(d), L(b), null));
        return d;
    }

    public Temp visit(BINOP n)
    {
        Exp l = n.left, r = n.right;
        switch (n.binop)
        {
        case BINOP.PLUS:
            if (isConst(r, 0)) { new Temp(); return l.accept(this); }
            if (isConst(l, 0)) { new Temp(); return r.accept(this); }
            if (const12(r)) return rri("addi", l, val(r));
            if (const12(l)) return rri("addi", r, val(l));
            return rrr("add", l, r);

        case BINOP.MINUS:
            if (isConst(r, 0)) { new Temp(); return l.accept(this); }
            if (const12(r) && val(r) != -2048) return rri("addi", l, -val(r));
            return rrr("sub", l, r);

        case BINOP.MUL:
        {
            int k;
            if ((k = log2(r)) > 0) return rri("sll", l, k);
            if ((k = log2(l)) > 0) return rri("sll", r, k);
            return rrr("mul", l, r);
        }

        case BINOP.DIV:                                  // reference: always div
            return rrr("div", l, r);

        case BINOP.AND:
        case BINOP.BITAND:
            if (const12(r)) return rri("andi", l, val(r));
            if (const12(l)) return rri("andi", r, val(l));
            return rrr("and", l, r);

        case BINOP.OR:
        case BINOP.BITOR:
            if (const12(r)) return rri("ori", l, val(r));
            if (const12(l)) return rri("ori", r, val(l));
            return rrr("or", l, r);

        case BINOP.BITXOR:
            if (const12(r)) return rri("xori", l, val(r));
            if (const12(l)) return rri("xori", r, val(l));
            return rrr("xor", l, r);

        case BINOP.LSHIFT:  return shift("sll", l, r);
        case BINOP.RSHIFT:  return shift("srl", l, r);
        case BINOP.ARSHIFT: return shift("sra", l, r);

        default:
            throw new Error("Codegen: unknown BINOP " + n.binop);
        }
    }

    private Temp shift(String op, Exp l, Exp r)
    {
        if (const12(r)) return rri(op, l, val(r));
        return rrr(op, l, r);
    }

    public Temp visit(CALL n)
    {
        Temp[] argRegs = RiscvFrame.argRegs();
        LinkedList<Temp> argUses = new LinkedList<Temp>();

        int i = 0;
        for (Exp arg : n.args)
        {
            Temp t = arg.accept(this);
            if (i < argRegs.length)
            {
                emit(new Assem.MOVE(fmt("mv", "`d0", "`s0"), argRegs[i], t));
                argUses.add(argRegs[i]);
            }
            else
            {
                int off = (i - argRegs.length) * frame.wordSize();
                emit(new OPER(fmt("sw", "`s0", off + "(`s1) //Tree.CALL"), null,
                              L(t, RiscvFrame.sp()), null));
                frame.maxArgOffset = Math.max(frame.maxArgOffset,
                                              off + frame.wordSize());
            }
            i++;
        }

        if (n.func instanceof NAME)
        {
            emit(new OPER(fmt("call", ((NAME) n.func).label.toString()),
                          RiscvFrame.calldefs(),
                          argUses.toArray(new Temp[0]), null));
        }
        else
        {
            Temp fn = n.func.accept(this);
            argUses.addFirst(fn);
            emit(new OPER(fmt("jalr", "`s0"), RiscvFrame.calldefs(),
                          argUses.toArray(new Temp[0]), null));
        }

        emit(new OPER("\t// Call sink", null, RiscvFrame.calldefs(), null));
        return frame.RV();
    }
}