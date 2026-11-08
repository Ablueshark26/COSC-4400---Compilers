package Absyn;

public class FieldExpr extends AssignableExpr {
    public Expr object;
    public String field;
    public int typeIndex = -1;
    public FieldExpr(Expr object, String field) {
        super();
        this.object = object;
        this.field = field;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
