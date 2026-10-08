package Absyn;
import java.util.LinkedList;
 
public class VoidDecl extends MethodDecl {
    public VoidDecl(String name, LinkedList<VarDecl> locals, LinkedList<Stmt> stmts) {
        super(null, false, name, new LinkedList<Formal>(), locals, stmts, new IntegerLiteral(0));
    }
    public void accept(Visitor v) { v.visit(this); }
}
