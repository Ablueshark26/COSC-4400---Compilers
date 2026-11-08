package Absyn;

public class TrueExpr extends Expr {
    public TrueExpr() {
        super();
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
