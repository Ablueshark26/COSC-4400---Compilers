package Absyn;
public class IntegerType extends Type{
	public void accept(Visitor v) { v.visit(this); }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

