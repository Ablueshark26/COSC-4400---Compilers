package Absyn;

import java.util.LinkedList;

public class NewArrayExpr extends Expr {
    public Type type;
    public LinkedList<Expr> sizes;

    public NewArrayExpr(Type type, LinkedList<Expr> sizes) {
        super();
        this.type = type;
        this.sizes = sizes;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

