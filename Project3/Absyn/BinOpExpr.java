package Absyn;

Public class BinaryOpExpr extends Expr;
	Expr e1, Expr e2;
	public BinaryOpExpr(Expr e1, Expr e2)
{
	this.e1 = e1;
	this.e2 = e2;
}


