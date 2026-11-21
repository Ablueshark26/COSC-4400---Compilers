package Absyn;

import java.util.List;

public class BlockStmt extends Stmt {
    public List<Stmt> stmts;

    public BlockStmt(List<Stmt> stmts) {
        super();
        this.stmts = stmts;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

