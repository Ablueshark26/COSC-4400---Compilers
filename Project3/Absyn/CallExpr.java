package Absyn;

import java.util.List;

public class CallExpr extends Expr {
    public Expr object;
    public String method;
    public List<Expr> args;

    public CallExpr(Expr object, String method, List<Expr> args) {
        super();
        this.object = object;
        this.method = method;
        this.args = args;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}

