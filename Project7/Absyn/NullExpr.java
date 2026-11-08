package Absyn;

public class NullExpr extends Expr {
    public NullExpr() {
        super();
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
