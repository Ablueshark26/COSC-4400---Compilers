/* Copyright (C) 2007, Marquette University.  All rights reserved. */
package Absyn;
import java.io.PrintWriter;

/**
 * Visitor prints AST in reparseable form.
 */

public class PrintVisitor implements Visitor
{
    PrintWriter out;
    public int indentCount = 0;

    public PrintVisitor(PrintWriter out)
    {
		this.out = out;
    }

    public PrintVisitor()
    {
		this.out = new PrintWriter(System.out);
    }

    private void indent()
    {
		out.print('\n');
		for(int i = 0; i < indentCount; i++)
	    { out.print(' '); }
    }

    /** Visitor pattern dispatch. */
    //public void visit(Absyn ast) {}

    public void visit(Program ast)
    {
		out.print("Program(");
		indentCount++;
		visit(ast.classes);
		indentCount--;
		out.println(")");
		out.flush();
    }

    public void visit(java.util.AbstractList list)
    {
		if (null == list)
	    {
			indent();
			out.print("null");
			return;
	    }
		indent();
		out.print("AbstractList(");
		indentCount++;
		for (Object o : list)
	    {
			if (null == o)
		    {   indent();   out.print("null");   }
			else
		    {   ((Visitable)o).accept(this);    }	
	    }
		out.print(")");
		indentCount--;
    }

    public void visit(ClassDecl ast)
    {
		indent();
		out.print("ClassDecl(");
		indentCount++;
		out.print(ast.name + " " + ast.parent); 
		visit(ast.fields);
		visit(ast.methods);
		indentCount--;
		out.print(")");
    }

    public void visit(MethodDecl ast)
    {
		indent();
		out.print("MethodDecl(");
		indentCount++;
		if (null != ast.returnType)
	    { ast.returnType.accept(this); }
		else
	    { out.print("public_static_void"); }
		if (ast.synced) { out.print(" synchronized"); }
		out.print(" " + ast.name);
		visit(ast.params);
        visit(ast.locals);
		visit(ast.stmts);
		ast.returnVal.accept(this);
		indentCount--;
		out.print(")");
    }
    
    public void visit(Formal ast)
    {
		indent();
		out.print("Formal(");
		ast.type.accept(this);
		out.print(" " + ast.name + ")");
    }

    public void visit(IdentifierType ast)
    {
		out.print("IdentifierType(" + ast.id + ")");
    }

    public void visit(VarDecl ast)
    {
		indent();
		out.print("VarDecl(");
		ast.type.accept(this);
		out.print(" " + ast.name);
		if (null == ast.init)
	    {   out.print(" null");   } 
		else
	    { ast.init.accept(this); }
		out.print(")");
    }

    public void visit(XinuCallStmt ast)
    {
		indent();
		out.print("XinuCallStmt(");
		indentCount++;
		out.print(ast.method);
		visit(ast.args);
		indentCount--;
		out.print(")");
    }

    public void visit(IntegerLiteral ast)
    {
		indent();
		out.print("IntegerLiteral(" + ast.value + ")");
    }

    public void visit(StringLiteral ast)
    {
		indent();
		out.print("StringLiteral(" + ast.value + ")");
    }

    public void visit(ArrayType ast)
    {
		out.print("ArrayType(");
		ast.base.accept(this);
		out.print(")");
    }
    public void visit(IntegerType ast)
    {
	        out.print("IntegerType");
    }
    public void visit(BooleanType ast)
    {
                out.print("BooleanType");
    }
    private void printBinOp(String name, BinOpExpr ast){
	    indent();
    	    out.print(name + "(");
	    indentCount ++; 
	    ast.e1.accept(this);
	    ast.e2.accept(this);
	    indentCount --;
	    out.print(")");
    }
    public void visit(AddExpr ast) { printBinOp("AddExpr", ast); }
    public void visit(SubExpr ast) { printBinOp("SubExpr", ast); }
    public void visit(MulExpr ast) { printBinOp("MulExpr", ast); }
    public void visit(DivExpr ast) { printBinOp("DivExpr", ast); }
    public void visit(LesserExpr ast) { printBinOp("LesserExpr", ast); }
    public void visit(GreaterExpr ast) { printBinOp("GreaterExpr", ast); }
    public void visit(EqualExpr ast) { printBinOp("EqualExpr", ast); }
    public void visit(NotEqExpr ast) { printBinOp("NotEqExpr", ast); }
    public void visit(AndExpr ast) { printBinOp("AndExpr", ast); }
    public void visit(OrExpr ast) { printBinOp("OrExpr", ast); }
    public void visit(IdentifierExpr ast){
	    indent();
	    out.print("IdentifierExpr(" + ast.name + ")");
    public void visit(NegExpr ast) {
	    indent();
	    out.print("NegExpr(");
    	    indentCount++;
            ast.e.accept(this);
    	    indentCount--;
            out.print(")");
    }

    public void visit(NotExpr ast) {
            indent();
            out.print("NotExpr(");
            indentCount++;
            ast.e.accept(this);
            indentCount--;
            out.print(")");
    }

    public void visit(NullExpr ast) {
            indent();
            out.print("NullExpr");
    }

    public void visit(NewArrayExpr ast) {
            indent();
            out.print("NewArrayExpr(");
            indentCount++;
            ast.type.accept(this);
            ast.size.accept(this);
            indentCount--;
            out.print(")");
    }

    public void visit(XinuCallExpr ast) {
            indent();
            out.print("XinuCallExpr(");
            indentCount++;
            out.print(ast.function);
            visit((java.util.AbstractList) ast.args);
            indentCount--;
            out.print(")");
    }

    public void visit(IfStmt ast) {
            indent();
            out.print("IfStmt(");
            indentCount++;
            ast.cond.accept(this);
            ast.thenStmt.accept(this);
            if (null == ast.elseStmt) { indent(); out.print("null"); }
            else { ast.elseStmt.accept(this); }
            indentCount--;
            out.print(")");
    }

    public void visit(WhileStmt ast) {
            indent();
            out.print("WhileStmt(");
            indentCount++;
            ast.cond.accept(this);
            ast.body.accept(this);
            indentCount--;
            out.print(")");
    }

    public void visit(BlockStmt ast) {
            indent();
            out.print("BlockStmt(");
            indentCount++;
            visit((java.util.AbstractList) ast.stmts);
            indentCount--;
            out.print(")");
    }

    public void visit(VoidDecl ast) {
            indent();
            out.print("VoidDecl(");
    	    indentCount++;
    	    out.print(ast.name);
    	    visit((java.util.AbstractList) ast.locals);
    	    visit((java.util.AbstractList) ast.body);
    	    indentCount--;
    	    out.print(")");
    }

    public void visit(ThreadDecl ast) {
       	    indent();
    	    out.print("ThreadDecl(");
    	    indentCount++;
    	    out.print(ast.name + " Thread");
    	    visit((java.util.AbstractList) ast.fields);
    	    LinkedList<Absyn> decls = new LinkedList<Absyn>();
    	    decls.addAll(ast.methods);
    	    decls.addAll(ast.voidMethods);
    	    visit(decls);
    	    indentCount--;
    	    out.print(")");
    }
}
