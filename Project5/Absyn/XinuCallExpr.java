package Absyn;

import java.util.List;

public class XinuCallExpr extends Expr {
    public String function;
    public List<Expr> args;

    public XinuCallExpr(String function, List<Expr> args) {
        super();
        this.function = function;
        this.args = args;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
