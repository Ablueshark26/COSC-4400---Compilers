/**
 * COSC 4400 - Project #7
 * Translate: visitor that converts the type-checked MiniJava abstract syntax
 * tree into Appel-style intermediate representation trees (Tree package).
 * Produces a list of fragments: one ProcFrag per method, and DataFrags for
 * string literals and per-class virtual tables.
 *
 * Runtime model:
 *   object : word 0 = vtable pointer, field i at byte offset (i+1)*wordSize
 *            (inherited fields first)
 *   array  : length at index -1,      element i at byte offset i*wordSize
 *   method : label Class__method, receiver ("this") is formal 0;
 *            static main is labelled "main" and has no receiver
 *   vtable : one word per method slot; overriding methods reuse the slot
 *   calls  : virtual dispatch through MEM(MEM(obj) + slot*wordSize)
 *   runtime: _new(bytes) (wraps malloc); Xinu.f(...) -> external call to f
 *
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
    /** Emit null-pointer checks (jump to frame.badPtr()) on object field access and calls. */
    public static boolean NULL_CHECKS = true;
    /** Emit array bounds checks (jump to frame.badSub()). */
    public static boolean BOUNDS_CHECKS = false;

    /** Prototype frame: used only as a factory for per-method frames. */
    public Frame.Frame frame;

    public Translate(Frame.Frame frame) { this.frame = frame; }

    private int ws() { return frame.wordSize(); }

    // ---------------------------------------------------------------
    // Tree-construction helpers
    // ---------------------------------------------------------------
    private static Tree.Exp CONST(int value)  { return new Tree.CONST(value); }
    private static Tree.Exp NAME(Label label) { return new Tree.NAME(label); }
    private static Tree.Exp TEMP(Temp temp)   { return new Tree.TEMP(temp); }
    private static Tree.Exp BINOP(int binop, Tree.Exp left, Tree.Exp right)
    {   return new Tree.BINOP(binop, left, right);   }
    private static Tree.Exp MEM(Tree.Exp exp) { return new Tree.MEM(exp); }
    private static Tree.Exp CALL(Tree.Exp func, List<Tree.Exp> args)
    {   return new Tree.CALL(func, args);   }
    private static Tree.Exp ESEQ(Tree.Stm stm, Tree.Exp exp)
    {
	if (stm == null) return exp;
	return new Tree.ESEQ(stm, exp);
    }
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

    /** Right-nested SEQ of a list of statements (null if empty). */
    private static Tree.Stm seqOf(List<Tree.Stm> stms)
    {
	Tree.Stm result = null;
	for (int i = stms.size() - 1; i >= 0; i--)
	    result = SEQ(stms.get(i), result);
	return result;
    }

    private Tree.Exp offset(Tree.Exp base, int bytes)
    {
	if (bytes == 0) return base;
	return BINOP(Tree.BINOP.PLUS, base, CONST(bytes));
    }

    // ---------------------------------------------------------------
    // Fragments and class/method environments
    // ---------------------------------------------------------------
    private LinkedList<Frag> frags = new LinkedList<Frag>();
    public List<Frag> results() { return frags; }

    /** Per-method information, one entry per vtable slot. */
    private static class MethodInfo
    {
	String name;
	Label label;
	Absyn.Type returnType;   // null for void methods
	String owner;            // class that supplies this implementation
    }

    /** Per-class layout information. */
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

    private static boolean isVoid(Absyn.MethodDecl m)
    {
	return m instanceof Absyn.VoidDecl || isMain(m);
    }

    private ClassInfo classInfo(String name)
    {
	if (name == null) return null;
	ClassInfo ci = classEnv.get(name);
	if (ci != null) return ci;
	Absyn.ClassDecl d = declOf.get(name);
	if (d == null) return null;            // built-in (Thread, String)
	ci = new ClassInfo();
	ci.name = name;
	ci.decl = d;
	classEnv.put(name, ci);                // before recursion: tolerate cycles
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
	    if (isMain(m)) continue;           // static: not in the vtable
	    MethodInfo mi = new MethodInfo();
	    mi.name = m.name;
	    mi.label = new Label(name + "__" + m.name);
	    mi.returnType = m.returnType;
	    mi.owner = name;
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

    // ---------------------------------------------------------------
    // Per-method state
    // ---------------------------------------------------------------
    private Table<Frame.Access> varEnv  = new Table<Frame.Access>();
    private Table<Absyn.Type>   typeEnv = new Table<Absyn.Type>();
    private Frame.Frame curFrame;
    private ClassInfo   curClass;
    private Frame.Access thisAccess;       // null in static main

    private Tree.Exp thisExp() { return thisAccess.exp(curFrame); }

    private Tree.Exp fieldAddr(Tree.Exp obj, ClassInfo ci, String field)
    {
	int idx = ci.fieldIndex.get(field);
	return offset(obj, (idx + 1) * ws());
    }

    // ---------------------------------------------------------------
    // Static types (only what translation needs: class of an expression)
    // ---------------------------------------------------------------
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

    // ---------------------------------------------------------------
    // Safety-check helpers
    // ---------------------------------------------------------------

    /** Make sure e can be used more than once: returns a TEMP (or CONST). */
    private Tree.Exp stable(Tree.Exp e, List<Tree.Stm> pre)
    {
	if (e instanceof Tree.TEMP || e instanceof Tree.CONST) return e;
	Temp t = new Temp();
	pre.add(MOVE(TEMP(t), e));
	return TEMP(t);
    }

    /** CJUMP(EQ, ptr, 0, badPtr, ok) followed by LABEL(ok), as in the lecture slides. */
    private Tree.Stm nullCheck(Tree.Exp ptr)
    {
	Label ok = new Label();
	return SEQ(CJUMP(Tree.CJUMP.EQ, ptr, CONST(0), frame.badPtr(), ok),
		   LABEL(ok));
    }

    private Tree.Stm boundsCheck(Tree.Exp array, Tree.Exp index)
    {
	Label bad = new Label();
	Label ok  = new Label();
	return SEQ(CJUMP(Tree.CJUMP.ULT, index, MEM(offset(array, -ws())), ok, bad),
		   SEQ(LABEL(bad),
		       SEQ(JUMP(frame.badSub()), LABEL(ok))));
    }

    private boolean isThis(Absyn.Expr e) { return e instanceof Absyn.ThisExpr; }

    // ---------------------------------------------------------------
    // Declarations
    // ---------------------------------------------------------------
    public Exp visit(Absyn.Program n)
    {
	for (Absyn.ClassDecl c : n.classes) declOf.put(c.name, c);
	for (Absyn.ClassDecl c : n.classes) classInfo(c.name);
	for (Absyn.ClassDecl c : n.classes) c.accept(this);
	return new Nx(null);
    }

    public Exp visit(Absyn.ClassDecl c)
    {
	curClass = classInfo(c.name);
	// vtable first, then the code of each method
	List<Label> labels = new ArrayList<Label>();
	for (MethodInfo mi : curClass.slots) labels.add(mi.label);
	boolean hasMain = false;
	for (Absyn.MethodDecl m : c.methods) if (isMain(m)) hasMain = true;
	if (!hasMain)     // the main class is never instantiated: no vtable
	    frags.add(new DataFrag(frame.record(curClass.vtable, labels)));
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
	boolean main = isMain(m);
	Label label = main ? new Label("main")
			   : new Label(curClass.name + "__" + m.name);
	curFrame = frame.newFrame(Symbol.symbol(label.toString()));

	varEnv.beginScope();
	typeEnv.beginScope();

	thisAccess = null;
	if (!main)
	    thisAccess = curFrame.allocFormal();     // receiver is formal 0
	for (Absyn.Formal f : m.params) f.accept(this);

	List<Tree.Stm> body = new ArrayList<Tree.Stm>();
	for (Absyn.VarDecl v : m.locals)
	{
	    Exp init = v.accept(this);
	    if (init != null && init.unNx() != null) body.add(init.unNx());
	}
	for (Absyn.Stmt s : m.stmts)
	    body.add(s.accept(this).unNx());

	// void methods and main end with MOVE(RV, 0)
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
	if (v.init == null) return new Nx(null);
	return new Nx(MOVE(a.exp(curFrame), v.init.accept(this).unEx()));
    }

    public Exp visit(Absyn.BooleanType  t)   { return new Nx(null); }
    public Exp visit(Absyn.IntegerType  t)   { return new Nx(null); }
    public Exp visit(Absyn.ArrayType t)      { return new Nx(null); }
    public Exp visit(Absyn.IdentifierType t) { return new Nx(null); }

    // ---------------------------------------------------------------
    // Statements
    // ---------------------------------------------------------------
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
	if (s.elseStmt != null)
	{
	    Exp elsePart = s.elseStmt.accept(this);
	    return new Nx(new IfThenElseExp(cond, thenPart, elsePart).unNx());
	}
	Label t = new Label();
	Label f = new Label();
	return new Nx(SEQ(cond.unCx(t, f),
			  SEQ(LABEL(t),
			      SEQ(thenPart.unNx(), LABEL(f)))));
    }

    public Exp visit(Absyn.WhileStmt s)
    {
	Label test = new Label();
	Label body = new Label();
	Label done = new Label();
	Exp cond = s.cond.accept(this);
	Exp loop = s.body.accept(this);
	return new Nx(SEQ(LABEL(test),
			  SEQ(cond.unCx(body, done),
			      SEQ(LABEL(body),
				  SEQ(loop.unNx(),
				      SEQ(JUMP(test), LABEL(done)))))));
    }

    // ---------------------------------------------------------------
    // Operators
    // ---------------------------------------------------------------
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
	// Short-circuit Boolean AND, with false == 0.
	return new IfThenElseExp(e1, e2, new Ex(CONST(0)));
    }

    public Exp visit(Absyn.OrExpr e)
    {
	Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	// Short-circuit Boolean OR, with true == 1.
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

    // ---------------------------------------------------------------
    // Allocation
    // ---------------------------------------------------------------
    public Exp visit(Absyn.NewObjectExpr e)
    {
	ClassInfo ci = classInfo(className(e.type));
	int nfields = (ci == null) ? 0 : ci.fieldIndex.size();
	Temp r = new Temp();
	List<Tree.Exp> args = new LinkedList<Tree.Exp>();
	args.add(CONST((nfields + 1) * ws()));
	Tree.Stm init = MOVE(TEMP(r), frame.externalCall(Symbol.symbol("new"), args));
	if (ci != null)
	    init = SEQ(init, MOVE(MEM(TEMP(r)), NAME(ci.vtable)));
	return new Ex(ESEQ(init, TEMP(r)));
    }

    /**
     * Array of n elements: _new((n+1) words), length stored in the first word,
     * result points at element 0 so the length lives at index -1.
     */
    private Tree.Exp initArray(Tree.Exp size)
    {
	Temp r = new Temp();
	Temp n = new Temp();
	List<Tree.Exp> args = new LinkedList<Tree.Exp>();
	args.add(BINOP(Tree.BINOP.MUL, BINOP(Tree.BINOP.PLUS, TEMP(n), CONST(1)),
		       CONST(ws())));
	Tree.Stm init =
	    SEQ(MOVE(TEMP(n), size),
	    SEQ(MOVE(TEMP(r), frame.externalCall(Symbol.symbol("new"), args)),
		MOVE(MEM(TEMP(r)), TEMP(n))));
	return ESEQ(init, BINOP(Tree.BINOP.PLUS, TEMP(r), CONST(ws())));
    }

    /** Allocate dims[k..], where trailing null dimensions are left unallocated. */
    private Tree.Exp newArray(List<Tree.Exp> dims, int k)
    {
	boolean last = (k + 1 >= dims.size()) || dims.get(k + 1) == null;
	if (last) return initArray(dims.get(k));

	Temp arr = new Temp();
	Temp i   = new Temp();
	Label test = new Label(), body = new Label(), done = new Label();
	Tree.Exp inner = newArray(dims, k + 1);
	Tree.Exp slot = BINOP(Tree.BINOP.PLUS, TEMP(arr),
			      BINOP(Tree.BINOP.MUL, TEMP(i), CONST(ws())));
	Tree.Stm loop =
	    SEQ(MOVE(TEMP(i), CONST(0)),
	    SEQ(LABEL(test),
	    SEQ(CJUMP(Tree.CJUMP.LT, TEMP(i), dims.get(k), body, done),
	    SEQ(LABEL(body),
	    SEQ(MOVE(MEM(slot), inner),
	    SEQ(MOVE(TEMP(i), BINOP(Tree.BINOP.PLUS, TEMP(i), CONST(1))),
	    SEQ(JUMP(test), LABEL(done))))))));
	return ESEQ(SEQ(MOVE(TEMP(arr), initArray(dims.get(k))), loop), TEMP(arr));
    }

    public Exp visit(Absyn.NewArrayExpr e)
    {
	// Evaluate every size exactly once, left to right.
	List<Tree.Stm> pre = new ArrayList<Tree.Stm>();
	List<Tree.Exp> dims = new ArrayList<Tree.Exp>();
	for (Absyn.Expr s : e.sizes)
	{
	    if (s == null) { dims.add(null); continue; }
	    dims.add(stable(s.accept(this).unEx(), pre));
	}
	Tree.Exp arr = newArray(dims, 0);
	return new Ex(ESEQ(seqOf(pre), arr));
    }

    // ---------------------------------------------------------------
    // Calls
    // ---------------------------------------------------------------
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
	List<Tree.Stm> pre = new ArrayList<Tree.Stm>();
	Tree.Exp obj = stable(e.object.accept(this).unEx(), pre);
	if (NULL_CHECKS && !isThis(e.object)) pre.add(nullCheck(obj));

	ClassInfo ci = classInfo(className(typeOf(e.object)));
	Integer slot = (ci == null) ? null : ci.slotOf.get(e.method);

	Tree.Exp call;
	if (slot == null)
	{
	    // Not a user-declared method (e.g. Thread.start): runtime routine.
	    call = frame.externalCall(Symbol.symbol(e.method), args(obj, e.args));
	}
	else
	{
	    Tree.Exp fn = MEM(offset(MEM(obj), slot * ws()));
	    call = CALL(fn, args(obj, e.args));
	}
	return new Ex(ESEQ(seqOf(pre), call));
    }

    // ---------------------------------------------------------------
    // Leaves
    // ---------------------------------------------------------------
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

    public Exp visit(Absyn.IdentifierExpr e)
    {
	Frame.Access a = varEnv.get(e.name);
	if (a != null) return new Ex(a.exp(curFrame));
	// otherwise a field of the current class (via this)
	if (curClass != null && curClass.fieldIndex.containsKey(e.name))
	    return new Ex(MEM(fieldAddr(thisExp(), curClass, e.name)));
	throw new Error("Translate: unbound identifier " + e.name);
    }

    public Exp visit(Absyn.FieldExpr e)
    {
	List<Tree.Stm> pre = new ArrayList<Tree.Stm>();
	Tree.Exp obj = stable(e.object.accept(this).unEx(), pre);
	Absyn.Type ot = typeOf(e.object);
	if (ot instanceof Absyn.ArrayType && e.field.equals("length"))
	{
	    return new Ex(ESEQ(seqOf(pre), MEM(offset(obj, -ws()))));
	}
	ClassInfo ci = classInfo(className(ot));
	if (ci == null || !ci.fieldIndex.containsKey(e.field))
	    throw new Error("Translate: cannot resolve field " + e.field);
	if (NULL_CHECKS && !isThis(e.object)) pre.add(nullCheck(obj));
	return new Ex(ESEQ(seqOf(pre), MEM(fieldAddr(obj, ci, e.field))));
    }

    public Exp visit(Absyn.ArrayExpr e)
    {
	List<Tree.Stm> pre = new ArrayList<Tree.Stm>();
	Tree.Exp arr = stable(e.array.accept(this).unEx(), pre);
	Tree.Exp idx = stable(e.index.accept(this).unEx(), pre);
	if (BOUNDS_CHECKS) pre.add(boundsCheck(arr, idx));
	Tree.Exp addr = BINOP(Tree.BINOP.PLUS, arr,
			      BINOP(Tree.BINOP.MUL, idx, CONST(ws())));
	return new Ex(ESEQ(seqOf(pre), MEM(addr)));
    }
}
