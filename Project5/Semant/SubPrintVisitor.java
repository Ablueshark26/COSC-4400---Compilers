package Semant;

import Absyn.*;
import Absyn.PrintVisitor;
import java.io.PrintWriter;

public class SubPrintVisitor extends PrintVisitor {

    public SubPrintVisitor(PrintWriter out) {
        super(out);
    }

    public void visit(VarDecl ast) {
        indent();
        out.print("VarDecl(");
        ast.type.accept(this);
        out.print(" " + ast.name);
        if (null == ast.init) { out.print(" null"); }
        else { ast.init.accept(this); }
        if (ast.checkType != null) {
            indentCount++;
            indent();
            ast.checkType.accept(new Types.PrintVisitor(out, indentCount));
            indentCount--;
        }
        out.print(")");
    }

    public void visit(MethodDecl ast) {
        indent();
        out.print("MethodDecl(");
        indentCount++;
        if (null != ast.returnType) { ast.returnType.accept(this); }
        else { out.print("public_static_void"); }
        if (ast.synced) { out.print(" synchronized"); }
        out.print(" " + ast.name);
        visit((java.util.AbstractList) ast.params);
        visit((java.util.AbstractList) ast.locals);
        visit((java.util.AbstractList) ast.stmts);
        ast.returnVal.accept(this);
        if (ast.checkType != null) {
            indent();
            ast.checkType.accept(new Types.PrintVisitor(out, indentCount));
        }
        indentCount--;
        out.print(")");
    }

    public void visit(ClassDecl ast) {
        indent();
        out.print("ClassDecl(");
        indentCount++;
        out.print(ast.name + " " + ast.parent);
        visit((java.util.AbstractList) ast.fields);
        visit((java.util.AbstractList) ast.methods);
        if (ast.checkType != null) {
            indent();
            ast.checkType.accept(new Types.PrintVisitor(out, indentCount));
        }
        indentCount--;
        out.print(")");
    }
}
