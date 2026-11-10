package Absyn;

public class ThisExpr extends Expr {
    public ThisExpr() {
        super();
    }

    public void accept(Visitor v) {
        v.visit(this);
    }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

