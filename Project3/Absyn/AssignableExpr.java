package Absyn;

// Expressions that can appear on the left hand side of an assignment.
public abstract class AssignableExpr extends Expr {
    public AssignableExpr() { super(); }
    public AssignableExpr(int line, int col) { super(line, col); }
}
