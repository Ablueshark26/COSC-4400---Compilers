package Absyn;

public class FalseExpr extends Expr {
    public FalseExpr() {
        super();
    }

    public void accept(Visitor v) {
        v.visit(this);
    }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

