package Absyn;

public class NewObjectExpr extends Expr {
    public Type type;
    public NewObjectExpr(Type type) {
        super();
        this.type = type;
    }
    public void accept(Visitor v) { v.visit(this); }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

