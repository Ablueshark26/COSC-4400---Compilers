package Absyn;

import java.util.List;

public class ThreadDecl extends Absyn {
    public String name;
    public List<VarDecl> fields;
    public List<MethodDecl> methods;
    public List<VoidDecl> voidMethods;

    public ThreadDecl(String name, List<VarDecl> fields, List<MethodDecl> methods, List<VoidDecl> voidMethods) {
        super();
        this.name = name;
        this.fields = fields;
        this.methods = methods;
        this.voidMethods = voidMethods;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
