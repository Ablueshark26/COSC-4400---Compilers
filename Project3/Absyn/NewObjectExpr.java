package Absyn;

public class NewObjectExpr extends Expr {
    public String className;

    public NewObjectExpr(String className) {
        super();
        this.className = className;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
