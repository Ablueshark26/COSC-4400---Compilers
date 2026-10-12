package Absyn;

// Expression indexing into an Array, e.g. arr[i].
public class ArrayExpr extends AssignableExpr {
    public Expr array;
    public Expr index;

    public ArrayExpr(Expr array, Expr index) {
        super();
        this.array = array;
        this.index = index;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}

