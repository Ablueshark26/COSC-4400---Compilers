Package Absyn;
 
public class AndExpr extends BinaryOpExpr{
	public AndExpr (Expr e1, Expr e2) {super(e1,e2);}
	Public void accept(Visitor v) {v.visit(this); }
}      
