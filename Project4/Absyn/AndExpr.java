package Absyn;
 
public class AndExpr extends BinOpExpr{
	public AndExpr (Expr e1, Expr e2) {super(e1,e2);}
	public void accept(Visitor v) {v.visit(this); }
}      
