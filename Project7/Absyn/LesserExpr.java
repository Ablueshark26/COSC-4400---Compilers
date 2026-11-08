package Absyn;
 
public class LesserExpr extends BinOpExpr{
	public LesserExpr (Expr e1, Expr e2) {super(e1,e2);}
	public void accept(Visitor v) {v.visit(this); }
} 
