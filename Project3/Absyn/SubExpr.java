Package Absyn;
 
public class AddExpr extends BinaryOpExpr{
	public AddExpr (Expr e1, Expr e2) {super(e1,e2);}
	Public void accept(Visitor v) {v.visit(this); }
}      
