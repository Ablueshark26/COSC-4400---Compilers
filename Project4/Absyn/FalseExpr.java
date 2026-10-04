package Absyn;

public class FalseExpr extends Expr {
    public FalseExpr() {
        super();
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
