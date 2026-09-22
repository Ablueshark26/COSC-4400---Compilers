/* Copyright (C) 2007, Marquette University.  All rights reserved. */
package Absyn;

/**
 * Interface for Visitor Pattern traversals.
 */

public interface Visitor
{
    /** Visitor pattern dispatch. */
    //    public void visit(Absyn ast);
    public void visit(java.util.AbstractList<Visitable> list);
    public void visit(ArrayType ast);
    public void visit(ClassDecl ast);
    public void visit(Formal ast);
    public void visit(IdentifierType ast);
    public void visit(IntegerLiteral ast);
    public void visit(MethodDecl ast);
    public void visit(Program ast);
    public void visit(StringLiteral ast);
    public void visit(VarDecl ast);
    public void visit(XinuCallStmt ast);
    public void visit(IntegerType ast);
    public void visit(BooleanType ast);
    public void visit(AddExpr ast);
    public void visit(SubExpr ast);
    public void visit(MulExpr ast);
    public void visit(DivExpr ast);
    public void visit(LesserExpr ast);
    public void visit(GreaterExpr ast);
    public void visit(EqualExpr ast);
    public void visit(NotEqExpr ast);
    public void visit(AndExpr ast);
    public void visit(OrExpr ast);    
    public void visit(IdentifierExpr ast);
}
