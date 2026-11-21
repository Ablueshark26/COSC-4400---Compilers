/**
 * COSC 4400 - Project #7
 * @authors Luke Sam
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
package Translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import Symbol.Symbol;
import Symbol.Table;
import Temp.Label;
import Temp.Temp;

public class Translate implements Absyn.TreeVisitor
{
    public Frame.Frame frame;

    public Translate(Frame.Frame frame) { this.frame = frame; }

    private int ws() { return frame.wordSize(); }

    private static Tree.Exp CONST(int value)  { return new Tree.CONST(value); }
    private static Tree.Exp NAME(Label label) { return new Tree.NAME(label); }
    private static Tree.Exp TEMP(Temp temp)   { return new Tree.TEMP(temp); }
    private static Tree.Exp BINOP(int binop, Tree.Exp left, Tree.Exp right)
    {   return new Tree.BINOP(binop, left, right);   }
    private static Tree.Exp MEM(Tree.Exp exp) { return new Tree.MEM(exp); }
    private static Tree.Exp CALL(Tree.Exp func, List<Tree.Exp> args)
    {   return new Tree.CALL(func, args);   }
    private static Tree.Exp ESEQ(Tree.Stm stm, Tree.Exp exp)
    {   return new Tree.ESEQ(stm, exp);   }
    private static Tree.Stm MOVE(Tree.Exp dst, Tree.Exp src)
    {   return new Tree.MOVE(dst, src);   }
    private static Tree.Stm EXP(Tree.Exp exp) { return new Tree.EXP(exp); }
    private static Tree.Stm JUMP(Label target) { return new Tree.JUMP(target); }
    private static Tree.Stm CJUMP(int relop, Tree.Exp l, Tree.Exp r,
				  Label t, Label f)
    {   return new Tree.CJUMP(relop, l, r, t, f);   }
    private static Tree.Stm SEQ(Tree.Stm left, Tree.Stm right)
    {
	if (left == null)  return right;
	if (right == null) return left;
	return new Tree.SEQ(left, right);
    }
    private static Tree.Stm LABEL(Label label) { return new Tree.LABEL(label); }

    private static Tree.Stm seqOf(List<Tree.Stm> stms)
    {
	Tree.Stm result = null;
	for (Tree.Stm s : stms) result = SEQ(result, s);
	return result;
    }

     private LinkedList<Frag> frags = new LinkedList<Frag>();
    public List<Frag> results() { return frags; }

    private static class MethodInfo
    {
	String name;
	Label label;
	Absyn.Type returnType;  
    }

    private static class ClassInfo
    {
	String name;
	Absyn.ClassDecl decl;
	ClassInfo parent;
	Label vtable;
	LinkedHashMap<String, Absyn.Type> fieldType = new LinkedHashMap<String, Absyn.Type>();
	LinkedHashMap<String, Integer>    fieldIndex = new LinkedHashMap<String, Integer>();
	ArrayList<MethodInfo> slots = new ArrayList<MethodInfo>();
	HashMap<String, Integer> slotOf = new HashMap<String, Integer>();
    }

    private HashMap<String, ClassInfo> classEnv = new HashMap<String, ClassInfo>();
    private HashMap<String, Absyn.ClassDecl> declOf = new HashMap<String, Absyn.ClassDecl>();

    private static boolean isMain(Absyn.MethodDecl m)
    {
	return m.returnType == null && !(m instanceof Absyn.VoidDecl);
    }

    private ClassInfo classInfo(String name)
    {
	if (name == null) return null;
	ClassInfo ci = classEnv.get(name);
	if (ci != null) return ci;
	Absyn.ClassDecl d = declOf.get(name);
	if (d == null) return null;          
	ci = new ClassInfo();
	ci.name = name;
	ci.decl = d;
	classEnv.put(name, ci);              
	ci.parent = classInfo(d.parent);
	ci.vtable = new Label(name + "__vtable");
	if (ci.parent != null)
	{
	    ci.fieldType.putAll(ci.parent.fieldType);
	    ci.fieldIndex.putAll(ci.parent.fieldIndex);
	    ci.slots.addAll(ci.parent.slots);
	    ci.slotOf.putAll(ci.parent.slotOf);
	}
	for (Absyn.VarDecl f : d.fields)
	{
	    ci.fieldIndex.put(f.name, ci.fieldIndex.size());
	    ci.fieldType.put(f.name, f.type);
	}
	for (Absyn.MethodDecl m : d.methods)
	{
	    MethodInfo mi = new MethodInfo();
	    mi.name = m.name;
	    mi.label = new Label(name + "__" + m.name);
	    mi.returnType = m.returnType;
	    Integer slot = ci.slotOf.get(m.name);
	    if (slot == null)
	    {
		ci.slotOf.put(m.name, ci.slots.size());
		ci.slots.add(mi);
	    }
	    else
		ci.slots.set(slot, mi);        // override reuses the slot
	}
	return ci;
    }

    private Table<Frame.Access> varEnv  = new Table<Frame.Access>();
    private Table<Absyn.Type>   typeEnv = new Table<Absyn.Type>();
    private Frame.Frame curFrame;
    private ClassInfo   curClass;
    private Frame.Access thisAccess;

    private Tree.Exp thisExp() { return thisAccess.exp(curFrame); }

    private Absyn.Type typeOf(Absyn.Expr e)
    {
	if (e instanceof Absyn.IdentifierExpr)
	{
	    String n = ((Absyn.IdentifierExpr) e).name;
	    Absyn.Type t = typeEnv.get(n);
	    if (t != null) return t;
	    if (curClass != null) return curClass.fieldType.get(n);
	    return null;
	}
	if (e instanceof Absyn.ThisExpr)
	    return curClass == null ? null : new Absyn.IdentifierType(curClass.name);
	if (e instanceof Absyn.NewObjectExpr)
	    return ((Absyn.NewObjectExpr) e).type;
	if (e instanceof Absyn.NewArrayExpr)
	{
	    Absyn.NewArrayExpr na = (Absyn.NewArrayExpr) e;
	    Absyn.Type t = na.type;
	    for (int i = 0; i < na.sizes.size(); i++) t = new Absyn.ArrayType(t);
	    return t;
	}
	if (e instanceof Absyn.FieldExpr)
	{
	    Absyn.FieldExpr fe = (Absyn.FieldExpr) e;
	    Absyn.Type ot = typeOf(fe.object);
	    if (ot instanceof Absyn.ArrayType) return new Absyn.IntegerType();
	    ClassInfo ci = classInfo(className(ot));
	    return ci == null ? null : ci.fieldType.get(fe.field);
	}
	if (e instanceof Absyn.ArrayExpr)
	{
	    Absyn.Type at = typeOf(((Absyn.ArrayExpr) e).array);
	    return (at instanceof Absyn.ArrayType) ? ((Absyn.ArrayType) at).base : null;
	}
	if (e instanceof Absyn.CallExpr)
	{
	    Absyn.CallExpr ce = (Absyn.CallExpr) e;
	    ClassInfo ci = classInfo(className(typeOf(ce.object)));
	    if (ci == null) return null;
	    Integer slot = ci.slotOf.get(ce.method);
	    return slot == null ? null : ci.slots.get(slot).returnType;
	}
	return null;
    }

    private static String className(Absyn.Type t)
    {
	return (t instanceof Absyn.IdentifierType) ? ((Absyn.IdentifierType) t).id : null;
    }

    public Exp visit(Absyn.Program n)
    {
	for (Absyn.ClassDecl c : n.classes) declOf.put(c.name, c);
	for (Absyn.ClassDecl c : n.classes) classInfo(c.name);

	for (Absyn.ClassDecl c : n.classes)
	{
	    ClassInfo ci = classInfo(c.name);
	    List<Label> labels = new ArrayList<Label>();
	    for (MethodInfo mi : ci.slots) labels.add(mi.label);
	    frags.add(new DataFrag(frame.record(ci.vtable, labels)));
	}
	for (Absyn.ClassDecl c : n.classes) c.accept(this);
	return new Nx(null);
    }

    public Exp visit(Absyn.ClassDecl c)
    {
	curClass = classInfo(c.name);
	for (Absyn.MethodDecl m : c.methods) m.accept(this);
	curClass = null;
	return new Nx(null);
    }

    public Exp visit(Absyn.ThreadDecl c)
    {
	return visit((Absyn.ClassDecl) c);
    }

    public Exp visit(Absyn.MethodDecl m)
    {
	String name = isMain(m) ? "main" : curClass.name + "__" + m.name;
	curFrame = frame.newFrame(Symbol.symbol(name));

	varEnv.beginScope();
	typeEnv.beginScope();

	thisAccess = curFrame.allocFormal();       // receiver is formal 0
	for (Absyn.Formal f : m.params) f.accept(this);

	List<Tree.Stm> body = new ArrayList<Tree.Stm>();
	for (Absyn.VarDecl v : m.locals)
	    body.add(v.accept(this).unNx());       // every local starts at 0
	for (Absyn.Stmt s : m.stmts)
	    body.add(s.accept(this).unNx());
	body.add(MOVE(TEMP(curFrame.RV()), m.returnVal.accept(this).unEx()));

	Tree.Stm result = seqOf(body);

	typeEnv.endScope();
	varEnv.endScope();

	frags.add(new ProcFrag(result, curFrame));
	curFrame = null;
	thisAccess = null;
	return new Nx(null);
    }

    public Exp visit(Absyn.VoidDecl m)
    {
	return visit((Absyn.MethodDecl) m);
    }

    public Exp visit(Absyn.Formal f)
    {
	Frame.Access a = curFrame.allocFormal();
	varEnv.put(f.name, a);
	typeEnv.put(f.name, f.type);
	return new Nx(null);
    }

    public Exp visit(Absyn.VarDecl v)
    {
	Frame.Access a = curFrame.allocLocal();
	varEnv.put(v.name, a);
	typeEnv.put(v.name, v.type);
	Tree.Exp init = (v.init == null) ? CONST(0) : v.init.accept(this).unEx();
	return new Nx(MOVE(a.exp(curFrame), init));
    }

    public Exp visit(Absyn.BooleanType  t)   { return new Nx(null); }
    public Exp visit(Absyn.IntegerType  t)   { return new Nx(null); }
    public Exp visit(Absyn.ArrayType t)      { return new Nx(null); }
    public Exp visit(Absyn.IdentifierType t) { return new Nx(null); }

    public Exp visit(Absyn.AssignStmt s)
    {
	Tree.Exp dst = s.lhs.accept(this).unEx();
	Tree.Exp src = s.rhs.accept(this).unEx();
	return new Nx(MOVE(dst, src));
    }

    public Exp visit(Absyn.BlockStmt b)
    {
	List<Tree.Stm> stms = new ArrayList<Tree.Stm>();
	for (Absyn.Stmt s : b.stmts)
	    stms.add(s.accept(this).unNx());
	return new Nx(seqOf(stms));
    }

    public Exp visit(Absyn.IfStmt s)
    {
	Exp cond = s.cond.accept(this);
	Exp thenPart = s.thenStmt.accept(this);
	Exp elsePart = (s.elseStmt == null) ? new Nx(null) : s.elseStmt.accept(this);
	return new Nx(new IfThenElseExp(cond, thenPart, elsePart).unNx());
    }

    public Exp visit(Absyn.WhileStmt s)
    {
	Exp cond = s.cond.accept(this);
	Exp loop = s.body.accept(this);
	Label done = new Label();
	Label test = new Label();
	Label body = new Label();
	return new Nx(SEQ(SEQ(SEQ(LABEL(test), cond.unCx(body, done)),
			      SEQ(SEQ(LABEL(body), loop.unNx()), JUMP(test))),
			  LABEL(done)));
    }

    private Exp Ex(int op, Absyn.BinOpExpr e)
    {
	Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	return new Ex(BINOP(op, e1.unEx(), e2.unEx()));
    }

    public Exp visit(Absyn.AddExpr e) { return Ex(Tree.BINOP.PLUS, e); }
    public Exp visit(Absyn.DivExpr e) { return Ex(Tree.BINOP.DIV, e); }
    public Exp visit(Absyn.MulExpr e) { return Ex(Tree.BINOP.MUL, e); }
    public Exp visit(Absyn.SubExpr e) { return Ex(Tree.BINOP.MINUS, e); }

    public Exp visit(Absyn.NegExpr e)
    {
	return new Ex(BINOP(Tree.BINOP.MINUS, CONST(0), e.e.accept(this).unEx()));
    }

    public Exp visit(Absyn.AndExpr e)
    {
	Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	return new IfThenElseExp(e1, e2, new Ex(CONST(0)));
    }

    public Exp visit(Absyn.OrExpr e)
    {
	Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	return new IfThenElseExp(e1, new Ex(CONST(1)), e2);
    }

    private Exp RelCx(int op, Absyn.BinOpExpr e)
    {
	Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	return new RelCx(op, e1.unEx(), e2.unEx());
    }

    public Exp visit(Absyn.GreaterExpr e)  { return RelCx(Tree.CJUMP.GT, e); }
    public Exp visit(Absyn.LesserExpr e)   { return RelCx(Tree.CJUMP.LT, e); }
    public Exp visit(Absyn.EqualExpr e)    { return RelCx(Tree.CJUMP.EQ, e); }
    public Exp visit(Absyn.NotEqExpr e)    { return RelCx(Tree.CJUMP.NE, e); }

    public Exp visit(Absyn.NotExpr e)
    {
	Exp exp = e.e.accept(this);
	return new IfThenElseExp(exp, new Ex(CONST(0)), new Ex(CONST(1)));
    }

    public Exp visit(Absyn.NewObjectExpr e)
    {
	ClassInfo ci = classInfo(className(e.type));
	int nfields = (ci == null) ? 0 : ci.fieldIndex.size();
	List<Tree.Exp> args = new LinkedList<Tree.Exp>();
	args.add(CONST(nfields));
	if (ci != null) args.add(NAME(ci.vtable));
	return new Ex(frame.externalCall(Symbol.symbol("new"), args));
    }

    private Tree.Exp initArray(Tree.Exp size)
    {
	Temp t = new Temp();
	List<Tree.Exp> args = new LinkedList<Tree.Exp>();
	args.add(TEMP(t));
	args.add(TEMP(t));
	return ESEQ(MOVE(TEMP(t), size),
		    frame.externalCall(Symbol.symbol("new"), args));
    }

    /** Allocate dims[k..], where trailing null dimensions are left unallocated. */
    private Tree.Exp newArray(List<Absyn.Expr> dims, int k)
    {
	boolean last = (k + 1 >= dims.size()) || dims.get(k + 1) == null;
	if (last) return initArray(dims.get(k).accept(this).unEx());

	Temp size = new Temp();
	Temp arr  = new Temp();
	Temp i    = new Temp();
	Label test = new Label(), body = new Label(), done = new Label();
	Tree.Stm first = MOVE(TEMP(size), dims.get(k).accept(this).unEx());
	Tree.Exp inner = newArray(dims, k + 1);
	Tree.Exp slot = BINOP(Tree.BINOP.PLUS, TEMP(arr),
			      BINOP(Tree.BINOP.MUL, TEMP(i), CONST(ws())));
	Tree.Stm loop =
	    SEQ(MOVE(TEMP(i), CONST(0)),
	    SEQ(LABEL(test),
	    SEQ(CJUMP(Tree.CJUMP.LT, TEMP(i), TEMP(size), body, done),
	    SEQ(LABEL(body),
	    SEQ(MOVE(MEM(slot), inner),
	    SEQ(MOVE(TEMP(i), BINOP(Tree.BINOP.PLUS, TEMP(i), CONST(1))),
	    SEQ(JUMP(test), LABEL(done))))))));
	return ESEQ(SEQ(first, SEQ(MOVE(TEMP(arr), initArray(TEMP(size))), loop)),
		    TEMP(arr));
    }

    public Exp visit(Absyn.NewArrayExpr e)
    {
	List<Absyn.Expr> dims = new ArrayList<Absyn.Expr>(e.sizes);
	return new Ex(newArray(dims, 0));
    }

    private List<Tree.Exp> args(Tree.Exp first, List<Absyn.Expr> actuals)
    {
	List<Tree.Exp> args = new LinkedList<Tree.Exp>();
	if (first != null) args.add(first);
	for (Absyn.Expr a : actuals) args.add(a.accept(this).unEx());
	return args;
    }

    public Exp visit(Absyn.XinuCallStmt s)
    {
	List<Tree.Exp> a = args(null, s.args);
	return new Nx(EXP(frame.externalCall(Symbol.symbol(s.method), a)));
    }

    public Exp visit(Absyn.XinuCallExpr e)
    {
	List<Tree.Exp> a = args(null, e.args);
	return new Ex(frame.externalCall(Symbol.symbol(e.function), a));
    }

    public Exp visit(Absyn.CallExpr e)
    {
	Tree.Exp obj = e.object.accept(this).unEx();
	ClassInfo ci = classInfo(className(typeOf(e.object)));
	Integer slot = (ci == null) ? null : ci.slotOf.get(e.method);

	List<Tree.Exp> actuals = args(null, e.args);

	Temp r = new Temp();
	Label ok = new Label();
	actuals.add(0, TEMP(r));

	Tree.Exp call;
	if (slot == null)
	    // Not a user-declared method (e.g. Thread.start): runtime routine.
	    call = frame.externalCall(Symbol.symbol(e.method), actuals);
	else
	{
	    Tree.Exp vtable = MEM(BINOP(Tree.BINOP.PLUS, TEMP(r), CONST(-ws())));
	    Tree.Exp fn = MEM(BINOP(Tree.BINOP.PLUS, vtable, CONST(slot * ws())));
	    call = CALL(fn, actuals);
	}
	return new Ex(ESEQ(SEQ(MOVE(TEMP(r), obj),
			       SEQ(CJUMP(Tree.CJUMP.EQ, TEMP(r), CONST(0),
					 frame.badPtr(), ok),
				   LABEL(ok))),
			   call));
    }

    public Exp visit(Absyn.FalseExpr e)      { return new Ex(CONST(0)); }
    public Exp visit(Absyn.TrueExpr e)       { return new Ex(CONST(1)); }
    public Exp visit(Absyn.IntegerLiteral e) { return new Ex(CONST(e.value)); }
    public Exp visit(Absyn.NullExpr e)       { return new Ex(CONST(0)); }

    private HashMap<String, Label> strings = new HashMap<String, Label>();

    /** Strip the surrounding quotes and decode the escapes of a lexed string. */
    private static String decode(String raw)
    {
	String s = raw;
	if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\""))
	    s = s.substring(1, s.length() - 1);
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < s.length(); i++)
	{
	    char c = s.charAt(i);
	    if (c != '\\' || i + 1 >= s.length()) { sb.append(c); continue; }
	    char d = s.charAt(++i);
	    switch (d)
	    {
	    case 'n': sb.append('\n'); break;
	    case 't': sb.append('\t'); break;
	    case 'b': sb.append('\b'); break;
	    case 'r': sb.append('\r'); break;
	    case 'f': sb.append('\f'); break;
	    case '\\': sb.append('\\'); break;
	    case '\'': sb.append('\''); break;
	    case '"': sb.append('"'); break;
	    default:
		if (d >= '0' && d <= '7')
		{
		    int v = d - '0';
		    int cnt = 1;
		    while (cnt < 3 && i + 1 < s.length()
			   && s.charAt(i + 1) >= '0' && s.charAt(i + 1) <= '7')
		    { v = v * 8 + (s.charAt(++i) - '0'); cnt++; }
		    sb.append((char) v);
		}
		else sb.append(d);
	    }
	}
	return sb.toString();
    }

    public Exp visit(Absyn.StringLiteral e)
    {
	String result = decode(e.value).intern();
	Label lab = strings.get(result);
	if (lab != null) return new Ex(NAME(lab));
	lab = new Label();
	strings.put(result, lab);
	frags.add(new DataFrag(frame.string(lab, result)));
	return new Ex(NAME(lab));
    }

    public Exp visit(Absyn.ThisExpr e)
    {
	return new Ex(thisExp());
    }

    private Exp fieldAccess(Tree.Exp obj, int index)
    {
	Label ok = new Label();
	Temp r = new Temp();
	return new Ex(ESEQ(SEQ(MOVE(TEMP(r), obj),
			       CJUMP(Tree.CJUMP.EQ, TEMP(r), CONST(0),
				     frame.badPtr(), ok)),
			   ESEQ(LABEL(ok),
				MEM(BINOP(Tree.BINOP.PLUS, TEMP(r),
					  CONST(index * ws()))))));
    }

    public Exp visit(Absyn.IdentifierExpr e)
    {
	Frame.Access a = varEnv.get(e.name);
	if (a != null) return new Ex(a.exp(curFrame));
	// otherwise a field of the current class (via this)
	if (curClass != null && curClass.fieldIndex.containsKey(e.name))
	    return fieldAccess(thisExp(), curClass.fieldIndex.get(e.name));
	throw new Error("Translate: unbound identifier " + e.name);
    }

    public Exp visit(Absyn.FieldExpr e)
    {
	Tree.Exp obj = e.object.accept(this).unEx();
	Absyn.Type ot = typeOf(e.object);
	if (ot instanceof Absyn.ArrayType && e.field.equals("length"))
	    return fieldAccess(obj, -1);
	ClassInfo ci = classInfo(className(ot));
	if (ci == null || !ci.fieldIndex.containsKey(e.field))
	    throw new Error("Translate: cannot resolve field " + e.field);
	return fieldAccess(obj, ci.fieldIndex.get(e.field));
    }

    public Exp visit(Absyn.ArrayExpr e)
    {
	Tree.Exp arr = e.array.accept(this).unEx();
	Tree.Exp idx = e.index.accept(this).unEx();
	Temp a = new Temp();
	Temp i = new Temp();
	Label nonNull = new Label();
	Label nonNeg  = new Label();
	Label inRange = new Label();
	Tree.Stm checks =
	    SEQ(MOVE(TEMP(a), arr),
	    SEQ(MOVE(TEMP(i), idx),
	    SEQ(CJUMP(Tree.CJUMP.EQ, TEMP(a), CONST(0), frame.badPtr(), nonNull),
	    SEQ(LABEL(nonNull),
	    SEQ(CJUMP(Tree.CJUMP.LT, TEMP(i), CONST(0), frame.badSub(), nonNeg),
	    SEQ(LABEL(nonNeg),
		CJUMP(Tree.CJUMP.GE, TEMP(i),
		      MEM(BINOP(Tree.BINOP.PLUS, TEMP(a), CONST(-ws()))),
		      frame.badSub(), inRange)))))));
	Tree.Exp elem = MEM(BINOP(Tree.BINOP.PLUS, TEMP(a),
				  BINOP(Tree.BINOP.MUL, TEMP(i), CONST(ws()))));
	return new Ex(ESEQ(checks, ESEQ(LABEL(inRange), elem)));
    }
}