package Absyn;

public class NegExpr extends Expr {
    public Expr e;

    public NegExpr(Expr e) {
        super();
        this.e = e;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
