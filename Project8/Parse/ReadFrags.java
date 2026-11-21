package Parse;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import Temp.Label;
import Temp.Temp;
import Translate.DataFrag;
import Translate.Frag;
import Translate.ProcFrag;

public class ReadFrags
{
    private final String in;
    private int pos = 0;

    private final HashMap<String, Label> labels = new HashMap<String, Label>();
    private final HashMap<String, Temp>  temps  = new HashMap<String, Temp>();

    public ReadFrags(Reader r)
    {
        for (Temp t : Riscv.RiscvFrame.allTemps()) temps.put(t.toString(), t);

        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int n;
        try { while ((n = r.read(buf)) > 0) sb.append(buf, 0, n); }
        catch (IOException e) { sb = new StringBuilder(); }
        in = sb.toString();
    }

    /* lexing */

    private FragParseException err(String msg)
    {
        int line = 1;
        for (int i = 0; i < pos && i < in.length(); i++)
            if (in.charAt(i) == '\n') line++;
        return new FragParseException(msg + " (line " + line + ")");
    }
    private void skipWs()
    {
        while (pos < in.length() && Character.isWhitespace(in.charAt(pos))) pos++;
    }

    private static boolean wordChar(char c)
    {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$'
            || c == '.' || c == '-';
    }

    /** Next token without consuming: "(", ")", ":", a word, or null at EOF. */
    private String peek() throws FragParseException
    {
        int save = pos;
        String t = next();
        pos = save;
        return t;
    }

    private String next() throws FragParseException
    {
        skipWs();
        if (pos >= in.length()) return null;
        char c = in.charAt(pos);
        if (c == '(' || c == ')' || c == ':') { pos++; return String.valueOf(c); }
        if (!wordChar(c)) throw err("unexpected character '" + c + "'");
        int start = pos;
        while (pos < in.length() && wordChar(in.charAt(pos))) pos++;
        return in.substring(start, pos);
    }

    private void expect(String tok) throws FragParseException
    {
        String t = next();
        if (t == null || !t.equals(tok))
            throw err("expected '" + tok + "' but found '" + t + "'");
    }

    private String word() throws FragParseException
    {
        String t = next();
        if (t == null || t.equals("(") || t.equals(")") || t.equals(":"))
            throw err("expected a name but found '" + t + "'");
        return t;
    }

    private int integer() throws FragParseException
    {
        String t = word();
        try { return Integer.parseInt(t); }
        catch (NumberFormatException e) { throw err("expected a number, found '" + t + "'"); }
    }

    private Label label(String name)
    {
        Label l = labels.get(name);
        if (l == null) { l = new Label(name); labels.put(name, l); }
        return l;
    }

    private Temp temp(String name) throws FragParseException
    {
        Temp t = temps.get(name);
        if (t != null) return t;
        if (name.length() < 2 || name.charAt(0) != 't')
            throw err("bad temp name '" + name + "'");
        try { t = new Temp(Integer.parseInt(name.substring(1))); }
        catch (NumberFormatException e) { throw err("bad temp name '" + name + "'"); }
        temps.put(name, t);
        return t;
    }

    /* grammar */

    public LinkedList<Frag> Program() throws FragParseException
    {
        LinkedList<Frag> frags = new LinkedList<Frag>();
        for (;;)
        {
            String t = peek();
            if (t == null) return frags;
            if (t.equals("DataFrag"))      frags.add(dataFrag());
            else if (t.equals("ProcFrag")) frags.add(procFrag());
            else throw err("expected DataFrag or ProcFrag but found '" + t + "'");
        }
    }

    private Frag dataFrag() throws FragParseException
    {
        expect("DataFrag");
        expect("(");
        int eol = in.indexOf('\n', pos);
        if (eol < 0) throw err("unterminated DataFrag");
        pos = eol + 1;
        StringBuilder text = new StringBuilder();
        boolean first = true;
        for (;;)
        {
            if (pos >= in.length()) throw err("unterminated DataFrag");
            int end = in.indexOf('\n', pos);
            String line = (end < 0) ? in.substring(pos) : in.substring(pos, end);
            pos = (end < 0) ? in.length() : end + 1;
            if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
            if (line.equals(")")) break;
            if (!first) text.append('\n');
            text.append(line);
            first = false;
        }
        return new DataFrag(text.toString());
    }

    private Frag procFrag() throws FragParseException
    {
        expect("ProcFrag");
        expect("(");
        Riscv.RiscvFrame frame = frame();
        Tree.Stm body = null;
        if (!")".equals(peek())) body = stm();
        expect(")");
        return new ProcFrag(body, frame);
    }

    private Riscv.RiscvFrame frame() throws FragParseException
    {
        expect("Riscv.RiscvFrame");
        expect("(");
        Riscv.RiscvFrame f = new Riscv.RiscvFrame();
        f.name = label(word());
        expect(":");
        f.formals = new LinkedList<Frame.Access>();
        f.actuals = new LinkedList<Frame.Access>();
        expect("Formals");
        accessList(f.formals);
        expect("Actuals");
        accessList(f.actuals);
        for (;;)
        {
            String t = peek();
            if (t != null && (t.equals("BadPtr") || t.equals("BadSub")))
            {
                next(); expect("("); word(); expect(")");
            }
            else break;
        }
        expect(")");
        return f;
    }

    private void accessList(java.util.List<Frame.Access> out) throws FragParseException
    {
        expect("(");
        for (;;)
        {
            String t = next();
            if (t == null) throw err("unterminated access list");
            if (t.equals(")")) return;
            expect("(");
            if (t.equals("InReg"))        out.add(new Riscv.InReg(temp(word())));
            else if (t.equals("InFrame")) out.add(new Riscv.InFrame(integer()));
            else throw err("expected InReg or InFrame but found '" + t + "'");
            expect(")");
        }
    }

    private Tree.Stm stm() throws FragParseException
    {
        String op = word();
        expect("(");
        Tree.Stm r;
        if (op.equals("SEQ"))
        {
            Tree.Stm a = stm();
            Tree.Stm b = stm();
            r = new Tree.SEQ(a, b);
        }
        else if (op.equals("LABEL"))
            r = new Tree.LABEL(label(word()));
        else if (op.equals("JUMP"))
        {
            Tree.Exp e = exp();
            LinkedList<Label> targets = new LinkedList<Label>();
            if (e instanceof Tree.NAME) targets.add(((Tree.NAME) e).label);
            r = new Tree.JUMP(e, targets);
        }
        else if (op.equals("CJUMP"))
        {
            int rel = relop(word());
            Tree.Exp a = exp();
            Tree.Exp b = exp();
            Label t = label(word());
            Label f = label(word());
            r = new Tree.CJUMP(rel, a, b, t, f);
        }
        else if (op.equals("MOVE"))
        {
            Tree.Exp d = exp();
            Tree.Exp s = exp();
            r = new Tree.MOVE(d, s);
        }
        else if (op.equals("EXP"))
            r = new Tree.EXP(exp());
        else
            throw err("expected a statement but found '" + op + "'");
        expect(")");
        return r;
    }

    private Tree.Exp exp() throws FragParseException
    {
        String op = word();
        expect("(");
        Tree.Exp r;
        if (op.equals("BINOP"))
        {
            int b = binop(word());
            Tree.Exp l = exp();
            Tree.Exp x = exp();
            r = new Tree.BINOP(b, l, x);
        }
        else if (op.equals("MEM"))
            r = new Tree.MEM(exp());
        else if (op.equals("TEMP"))
            r = new Tree.TEMP(temp(word()));
        else if (op.equals("ESEQ"))
        {
            Tree.Stm s = stm();
            Tree.Exp e = exp();
            r = new Tree.ESEQ(s, e);
        }
        else if (op.equals("NAME"))
            r = new Tree.NAME(label(word()));
        else if (op.equals("CONST"))
            r = new Tree.CONST(integer());
        else if (op.equals("CALL"))
        {
            Tree.Exp fn = exp();
            LinkedList<Tree.Exp> args = new LinkedList<Tree.Exp>();
            while (!")".equals(peek())) args.add(exp());
            r = new Tree.CALL(fn, args);
        }
        else
            throw err("expected an expression but found '" + op + "'");
        expect(")");
        return r;
    }

    private int binop(String s) throws FragParseException
    {
        switch (s)
        {
        case "PLUS":    return Tree.BINOP.PLUS;
        case "MINUS":   return Tree.BINOP.MINUS;
        case "MUL":     return Tree.BINOP.MUL;
        case "DIV":     return Tree.BINOP.DIV;
        case "AND":     return Tree.BINOP.AND;
        case "OR":      return Tree.BINOP.OR;
        case "LSHIFT":  return Tree.BINOP.LSHIFT;
        case "RSHIFT":  return Tree.BINOP.RSHIFT;
        case "ARSHIFT": return Tree.BINOP.ARSHIFT;
        case "BITAND":  return Tree.BINOP.BITAND;
        case "BITOR":   return Tree.BINOP.BITOR;
        case "BITXOR":  return Tree.BINOP.BITXOR;
        }
        throw err("unknown BINOP '" + s + "'");
    }


    private int relop(String s) throws FragParseException
    {
        switch (s)
        {
        case "EQ":  return Tree.CJUMP.EQ;
        case "NE":  return Tree.CJUMP.NE;
        case "LT":  return Tree.CJUMP.LT;
        case "GT":  return Tree.CJUMP.GT;
        case "LE":  return Tree.CJUMP.LE;
        case "GE":  return Tree.CJUMP.GE;
        case "ULT": return Tree.CJUMP.ULT;
        case "ULE": return Tree.CJUMP.ULE;
        case "UGT": return Tree.CJUMP.UGT;
        case "UGE": return Tree.CJUMP.UGE;
        }
        throw err("unknown relational operator '" + s + "'");
    }
}

