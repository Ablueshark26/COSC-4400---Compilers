package Absyn;

public class NewArrayExpr extends Expr {
    public Type type;
    public Expr size;

    public NewArrayExpr(Type type, Expr size) {
        super();
        this.type = type;
        this.size = size;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
