package Absyn;

public class BinOpExpr extends Expr{
	Expr e1, e2;
	public BinOpExpr(Expr e1, Expr e2)
{
	this.e1 = e1;
	this.e2 = e2;
}
	public void accept(Visitor v) {}
}


