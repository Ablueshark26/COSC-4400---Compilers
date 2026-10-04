package Absyn;

public class NotEqExpr extends BinOpExpr{
	public NotEqExpr (Expr e1, Expr e2) {super(e1,e2);}
	public void accept(Visitor v) {v.visit(this); }
} 
