Package Absyn;

public class NotEqExpr extends BinaryOpExpr{
	public NotEqExpr (Expr e1, Expr e2) {super(e1,e2);}
	Public void accept(Visitor v) {v.visit(this); }
} 
