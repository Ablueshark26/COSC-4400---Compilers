package Absyn;
import java.util.LinkedList;

public class ThreadDecl extends ClassDecl {
    public ThreadDecl(String name, LinkedList<VarDecl> fields, LinkedList<MethodDecl> methods) {
        super(name, "Thread", fields, methods);
    }
    public void accept(Visitor v) { v.visit(this); }

    /** Translation visitor dispatch. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}

