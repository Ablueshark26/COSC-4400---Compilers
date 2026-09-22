package Absyn;

public class EqualExpr extends BinOpExpr{
	public EqualExpr (Expr e1, Expr e2) {super(e1,e2);}
	public void accept(Visitor v) {v.visit(this); }
}   
