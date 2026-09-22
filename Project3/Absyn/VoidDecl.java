package Absyn;

import java.util.List;

public class VoidDecl extends Absyn {
    public String name;
    public List<VarDecl> locals;
    public List<Stmt> body;

    public VoidDecl(String name, List<VarDecl> locals, List<Stmt> body) {
        super();
        this.name = name;
        this.locals = locals;
        this.body = body;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
