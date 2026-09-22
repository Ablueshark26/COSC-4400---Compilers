Package Absyn;
 
public class DivExpr extends BinaryOpExpr{
	public DivExpr (Expr e1, Expr e2) {super(e1,e2);}
	Public void accept(Visitor v) {v.visit(this); }
}    
