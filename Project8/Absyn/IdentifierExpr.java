package Absyn;
public class IdentifierExpr extends AssignableExpr {
    public String name;
    public IdentifierExpr(String name) { this.name = name; }
    public void accept(Visitor v) { v.visit(this); }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

