package Absyn;

public class TrueExpr extends Expr {
    public TrueExpr() {
        super();
    }

    public void accept(Visitor v) {
        v.visit(this);
    }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

