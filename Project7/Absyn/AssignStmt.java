package Absyn;

public class AssignStmt extends Stmt {
    public AssignableExpr lhs;
    public Expr rhs;

    public AssignStmt(AssignableExpr lhs, Expr rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
