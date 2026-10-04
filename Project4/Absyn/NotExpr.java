package Absyn;

public class NotExpr extends Expr {
    public Expr e;

    public NotExpr(Expr e) {
        super();
        this.e = e;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
