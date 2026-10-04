package Absyn;
public class IntegerType extends Type{
	public void accept(Visitor v) { v.visit(this); }
}
