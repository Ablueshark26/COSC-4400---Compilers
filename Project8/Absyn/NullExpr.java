package Absyn;

public class NullExpr extends Expr {
    public NullExpr() {
        super();
    }

    public void accept(Visitor v) {
        v.visit(this);
    }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

