package Absyn;
 
public class DivExpr extends BinOpExpr{
	public DivExpr (Expr e1, Expr e2) {super(e1,e2);}
	public void accept(Visitor v) {v.visit(this); }
}    
