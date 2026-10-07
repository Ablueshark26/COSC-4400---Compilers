package Semant;

import Absyn.*;
import Symbol.Table;
import Types.ARRAY;
import Types.BOOLEAN;
import Types.CLASS;
import Types.FIELD;
import Types.FUNCTION;
import Types.INT;
import Types.NIL;
import Types.OBJECT;
import Types.RECORD;
import Types.STRING;
import Types.VOID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeChecker implements Visitor {

	static final boolean SUPPRESS_CASCADES = true;

    //Type given to erroneous expressions when SUPPRESS_CASCADES is on.
	static class ERR extends Types.Type {
        	public boolean coerceTo(Types.Type t) { return true; }
        	public boolean equals(Object o) { return true; }
        	public int hashCode() { return 0; }
        	public String toString() { return "?"; }
        	public void accept(Types.Visitor v) { }
    	}

	//State
	private final Table<CLASS> classes = new Table<CLASS>();   // class descriptors
    	private Table<Types.Type> vars = new Table<Types.Type>();  // locals/formals, scoped
    	private final Map<MethodDecl, FUNCTION> fnOf = new IdentityHashMap<MethodDecl, FUNCTION>();
    	private final Map<Absyn, CLASS> descOf = new IdentityHashMap<Absyn, CLASS>();

    	private CLASS cur;              // class whose members are being checked
    	private Types.Type result;      // result of the last expression/type visited
    	int errors = 0;
	
	//Helpers
	private void error(String msg) {
        	errors++;
        	System.out.println("ERROR " + msg);
    	}

    	private Types.Type errType() {
        	return SUPPRESS_CASCADES ? new ERR() : new INT();
    	}

    	private static boolean isErr(Types.Type t) { return t instanceof ERR; }

    	private Types.Type check(Expr e) {
        	e.accept(this);
        	return result;
    	}

    	private Types.Type resolve(Type t) {
        	t.accept(this);
        	return result;
   	}

    	private boolean ok(Types.Type from, Types.Type to) {
        	return isErr(from) || isErr(to) || from.coerceTo(to);
    	}

    	private void expect(Types.Type required, Types.Type found) {
        	if (!ok(found, required)) error("incompatible types: " + required + " required, but " + found + " found");
    	}

    	private static boolean isRef(Types.Type t) {
        	return t instanceof OBJECT || t instanceof ARRAY || t instanceof NIL;
    	}

	private static FIELD findField(CLASS c, String name) //this scans the parent to find the field
	{
		for(CLASS curr = c; curr != null; curr = curr.parent) //start at given class move up
		{
			FIELD f = curr.fields.get(name);
			if (f != null) return f;
		}
		return null;
	}

	private static FUNCTION findFunc(CLASS c, String name) //this scans the parent to find the function. similar to first
        {
                for(CLASS curr = c; curr != null; curr = curr.parent) //start at given class move up
                {
                        FIELD f = curr.fields.get(name); //stores inside field
                        if (f != null) return (FUNCTION) f.type; 
                }
                return null;
        }

	//Build 
	public void visit(Program n) {
        	List<Absyn> decls = (List<Absyn>) n.classes;

        	//The built-in superclass of every thread.
        	CLASS thread = new CLASS("Thread");
        	classes.put("Thread", thread);

        	//enter every class name
        	for (Absyn d : decls) {
            	String name = nameOf(d);
            	if (classes.get(name) != null) {
                	error("duplicate class");
                	continue;
            	}
            	CLASS c = new CLASS(name);
            	classes.put(name, c);
            	descOf.put(d, c);
        	}

        	//resolve superclasses
        	for (Absyn d : decls) {
            		CLASS c = descOf.get(d);
            		if (c == null) continue;
            		if (d instanceof ThreadDecl) {
                		c.parent = thread;
            		} 
			else {
                		String sup = ((ClassDecl) d).parent;
                		if (sup != null) {
                    			CLASS p = classes.get(sup);
                    			if (p == null) error("cannot resolve parent class " + sup);
                    			else c.parent = p;
                		}
            		}
        	}

        	//cyclic inheritance reported once per cycle, then the cycle is cut
        	for (Absyn d : decls) {
            		CLASS c = descOf.get(d);
            		if (c == null) continue;
            		Set<CLASS> seen = new HashSet<CLASS>();
            		seen.add(c);
            		for (CLASS p = c.parent; p != null; p = p.parent) {
                		if (p == c) {
                    			error("cyclic inheritance involving " + c.name);
                    			c.parent = null;
                    			break;
                		}
                	if (!seen.add(p)) break;   // a cycle that does not include c
            		}
        	}
		//fields and methods
        	for (Absyn d : decls) {
            		CLASS c = descOf.get(d);
            		if (c == null) continue;
            		List<VarDecl> fields;
            		List<MethodDecl> methods;
            		List<VoidDecl> voids = null;
            		if (d instanceof ClassDecl) {
                		fields = ((ClassDecl) d).fields;
                		methods = ((ClassDecl) d).methods;
            		} 
			else {
                		fields = ((ThreadDecl) d).fields;
                		methods = ((ThreadDecl) d).methods;
                		voids = ((ThreadDecl) d).voidMethods;
            		}
            		for (VarDecl f : fields) {
                		Types.Type t = resolve(f.type);
                		if (c.fields.get(f.name)!= null) error(f.name + " is already defined in " + c.name);
                		else c.fields.put(t, f.name);
            		}
            		for (MethodDecl m : methods) {
                		if (m.returnType == null) continue;            // main is not a member
                		Types.Type ret = resolve(m.returnType);
                		List<Types.Type> ps = new ArrayList<Types.Type>();
                		List<String> pn = new ArrayList<String>();
                		for (Formal f : m.params) {
                    			ps.add(resolve(f.type));
                    			pn.add(f.name);
                		}
                		FUNCTION fn = new FUNCTION(m.name, c.instance, new RECORD(), ret);
                		fnOf.put(m, fn);
                		if (c.methods.get(m.name) != null) error(m.name + " is already defined in " + c.name);
                		else c.methods.put(fn, m.name);
            		}
            		if (voids != null) {
                		for (VoidDecl v : voids) {
                    			if (c.methods.get(v.name)!= null) error(v.name + " is already defined in " + c.name);
				
                    		else {
					FUNCTION fn = new FUNCTION(v.name, c.instance, new RECORD(), new VOID());
                			c.methods.put(fn, v.name);
				}
            		}
        	}
		}
		//overrides must keep the signature
        	for (Absyn d : decls) {
            		CLASS c = descOf.get(d);
            		if (c == null || c.parent == null) continue;
            		for (FIELD mf : c.methods) {
				FUNCTION fn = (FUNCTION) mf.type;
                		FUNCTION inherited = findFunc(c.parent, fn.name);
                		if (inherited != null && !inherited.coerceTo(fn))
                    			error("incompatible method override: " + fn.name + " in class " + c.name);
            		}
        	}

        	//bodies
        	for (Absyn d : decls) {
            		if (descOf.get(d) != null) d.accept(this);
        	}
    	}


    	private static String nameOf(Absyn d) {
        	return d instanceof ClassDecl ? ((ClassDecl) d).name : ((ThreadDecl) d).name;
    	}
		public void visit(java.util.AbstractList<Visitable> list){} //needed to reference later
									     
		//Declarations
		public void visit(ClassDecl n) {
        		cur = descOf.get(n);
        		for (MethodDecl m : n.methods) m.accept(this);
    		}

    		public void visit(ThreadDecl n) {
        		cur = descOf.get(n);
        		for (MethodDecl m : n.methods) m.accept(this);
        		for (VoidDecl v : n.voidMethods) v.accept(this);
    		}

    		private void declare(String name, Types.Type t, String owner) {
        		if (vars.inCurrentScope(name)) error(name + " is already defined in " + owner);
        		else vars.put(name, t);
    		}

    		private void declareLocals(List<VarDecl> locals, String owner) {
        		for (VarDecl v : locals) {
            			Types.Type vt = resolve(v.type);
            			declare(v.name, vt, owner);
            			if (v.init != null) expect(vt, check(v.init));
        		}
    		}

    		public void visit(MethodDecl n) {
        		FUNCTION fn = fnOf.get(n);          // null for main
        		vars.beginScope();
        		for (Formal f: n.params) {
				declare(f.name, resolve(f.type), n.name);
			}
        		declareLocals(n.locals, n.name);
        		for (Stmt s : n.stmts) s.accept(this);
        		if (fn != null) expect(fn.result, check(n.returnVal));
        		vars.endScope();
    		}

    	public void visit(VoidDecl n) {
        	vars.beginScope();
        	declareLocals(n.locals, n.name);
        	for (Stmt s : n.body) s.accept(this);
        	vars.endScope();
    	}

    	public void visit(VarDecl n) { }
    	public void visit(Formal n) { }

		//types
	
		public void visit(IntegerType n) { result = new INT(); }
    		public void visit(BooleanType n) { result = new BOOLEAN(); }
    		public void visit(ArrayType n) { result = new ARRAY(resolve(n.base)); }

    		public void visit(IdentifierType n) {
        		if (n.id.equals("String")) { result = new STRING(); return; }
        		CLASS c = classes.get(n.id);
        		if (c == null) {
            			error("cannot resolve class " + n.id);
            			result = errType();
        		} 
			else {
            			result = new OBJECT(c);
        		}
    		}

		//Statements
	
		public void visit(BlockStmt n) {
        		vars.beginScope();
        		for (Stmt s : n.stmts) s.accept(this);
        		vars.endScope();
    		}

    		public void visit(IfStmt n) {
        		expect(new BOOLEAN(), check(n.cond));
        		n.thenStmt.accept(this);
        		if (n.elseStmt != null) n.elseStmt.accept(this);
    		}

    		public void visit(WhileStmt n) {
        		expect(new BOOLEAN(), check(n.cond));
        		n.body.accept(this);
    		}

    		public void visit(AssignStmt n) {
        		Types.Type l = check(n.lhs);
        		Types.Type r = check(n.rhs);
        		expect(l, r);
    		}

    		public void visit(XinuCallStmt n) {
        		for (Expr a : n.args) check(a);
    		}

		//Operators
	
		private void arith(BinOpExpr n, String op, Types.Type operand, Types.Type res) {
        		Types.Type l = check(n.e1);
        		Types.Type r = check(n.e2);
        		if (!ok(l, operand) || !ok(r, operand))
            		error("operator " + op + " cannot be applied to " + l + ", " + r);
        		result = res;
    		}

    		public void visit(AddExpr n) { arith(n, "+", new INT(), new INT()); }
    		public void visit(SubExpr n) { arith(n, "-", new INT(), new INT()); }
    		public void visit(MulExpr n) { arith(n, "*", new INT(), new INT()); }
    		public void visit(DivExpr n) { arith(n, "/", new INT(), new INT()); }
    		public void visit(AndExpr n) { arith(n, "&&", new BOOLEAN(), new BOOLEAN()); }
    		public void visit(OrExpr n) { arith(n, "||", new BOOLEAN(), new BOOLEAN()); }
    		public void visit(GreaterExpr n) { arith(n, ">", new INT(), new BOOLEAN()); }
    		public void visit(LesserExpr n) { arith(n, "<", new INT(), new BOOLEAN()); }

    		private void equality(BinOpExpr n, String op) {
        		Types.Type l = check(n.e1);
        		Types.Type r = check(n.e2);
        		boolean good = isErr(l) || isErr(r)
            		|| (isRef(l) && isRef(r) && (l.coerceTo(r) || r.coerceTo(l)))
            		|| (!isRef(l) && !isRef(r) && l.equals(r));
        		if (!good) error("operator " + op + " cannot be applied to " + l + ", " + r);
        		result = new BOOLEAN();
    		}

    		public void visit(EqualExpr n) { equality(n, "=="); }
    		public void visit(NotEqExpr n) { equality(n, "!="); }

    		public void visit(NegExpr n) {
        		Types.Type t = check(n.e);
        		if (!ok(t, new INT())) error("operator - cannot be applied to " + t);
        		result = new INT();
    		}

    		public void visit(NotExpr n) {
        		Types.Type t = check(n.e);
        		if (!ok(t, new BOOLEAN())) error("operator ! cannot be applied to " + t);
        		result = new BOOLEAN();
    		}

		//Simple expressions
	
		public void visit(IntegerLiteral n) { result = new INT(); }
    		public void visit(StringLiteral n) { result = new STRING(); }
    		public void visit(TrueExpr n) { result = new BOOLEAN(); }
    		public void visit(FalseExpr n) { result = new BOOLEAN(); }
    		public void visit(NullExpr n) { result = new NIL(); }
    		public void visit(ThisExpr n) { result = new OBJECT(cur); }

    		public void visit(IdentifierExpr n) {
        		Types.Type t = vars.get(n.name);
        		if (t == null) {
            			FIELD f = findField(cur, n.name);
            			if (f != null) t = f.type;
        		}
        		if (t == null) {	
            			error("cannot resolve symbol " + n.name);
            			t = errType();
        		}
        		result = t;
    		}

    		public void visit(FieldExpr n) {
        		Types.Type ot = check(n.object);
        		if (isErr(ot)) { result = errType(); return; }
        		if (ot instanceof ARRAY && n.field.equals("length")) { result = new INT(); return; }
        		if (!(ot instanceof OBJECT)) {
            			error("target not object, type " + ot);
            			result = errType();
            			return;
        		}
        		FIELD f = findField(((OBJECT) ot).myClass, n.field);
        		if (f == null) {
            			error("cannot resolve symbol " + n.field);
            			result = errType();
        		} 
			else {
            			result = f.type;
        		}
    		}

		public void visit(ArrayExpr n) {
        		Types.Type at = check(n.array);
        		Types.Type it = check(n.index);
        		Types.Type elem;
        		if (isErr(at)) {
            			elem = errType();
        		} else if (at instanceof ARRAY) {
            			elem = ((ARRAY) at).element;
        		} else {
            			error("incompatible types: array required, but " + at + " found");
            			elem = errType();
        		}
        		expect(new INT(), it);
        		result = elem;
    		}

    		public void visit(CallExpr n) {
        		Types.Type ot = check(n.object);
        		FUNCTION fn = null;
        		if (ot instanceof OBJECT) {
            			fn = findFunc(((OBJECT) ot).myClass, n.method);
            			if (fn == null) error("cannot resolve method " + n.method);
        		} else if (!isErr(ot)) {
            			error("target not object, type " + ot);
        		}
        		List<Types.Type> ats = new ArrayList<Types.Type>();
        		for (Expr a : n.args) ats.add(check(a));
        		if (fn != null) {
            			if (ats.size() != fn.formals.size()) {
                			error("mismatch in number of arguments");
            			} 
				else {
					Iterator<FIELD> it = fn.formals.iterator();
                			for (Types.Type at : ats){
						FIELD pf = it.next();
						expect(pf.type,at);
				        }
            			}
        		}
        		result = (fn != null) ? fn.result : errType();
    		}

    		public void visit(XinuCallExpr n) {
        		for (Expr a : n.args) check(a);
        		result = new INT();
    		}

    		public void visit(NewArrayExpr n) {
        		Types.Type t = resolve(n.type);
        		for (Expr s : n.sizes) {
            			if (s != null) expect(new INT(), check(s));
        		}
        		for (int i = 0; i < n.sizes.size(); i++) t = new ARRAY(t);
        		result = t;
    		}

		public void visit(NewObjectExpr n) {
        		CLASS c = classes.get(n.className);
        		if (c == null) {
            			error("cannot resolve class " + n.className);
            			result = errType();
        		} 
			else {
            			result = new OBJECT(c);
        		}
    		}

}

