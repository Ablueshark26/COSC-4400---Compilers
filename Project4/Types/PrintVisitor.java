package Types;

 
import java.io.PrintWriter;
 
public class PrintVisitor implements Visitor
{
    private PrintWriter out;
    private int indentCount = 0;
 
    public PrintVisitor(PrintWriter out) { this.out = out; }
 
    private void indent()
    {
        out.print('\n');
        for (int i = 0; i < indentCount; i++) out.print(' ');
    }
 
    public void visit(CLASS c)
    {
        out.print("CLASS(" + c.name);
        indentCount++;
        indent();
        if (c.parent == null) out.print("null");
        else out.print(c.parent.name);  
        indent(); c.methods.accept(this);
        indent(); c.fields.accept(this);
        indent(); c.instance.accept(this);
        indentCount--;
        out.print(")");
    }
 
    public void visit(RECORD r)
    {
        out.print("RECORD(");
        indentCount++;
        for (FIELD f : r) {
            indent();
            f.accept(this);
        }
        indentCount--;
        out.print(")");
    }
 
    public void visit(FIELD f)
    {
        out.print("FIELD(" + f.index + " " + f.name);
        indentCount++;
        indent();
        f.type.accept(this);
        indentCount--;
        out.print(")");
    }
 
    public void visit(FUNCTION fn)
    {
        out.print("FUNCTION(" + fn.name);
        indentCount++;
        indent(); fn.self.accept(this);
        indent(); fn.formals.accept(this);
        indent(); fn.result.accept(this);
        indentCount--;
        out.print(")");
    }
 
    public void visit(ARRAY a)
    {
        out.print("ARRAY(");
        indentCount++;
        indent();
        a.element.accept(this);
        indentCount--;
        out.print(")");
    }
 
    public void visit(OBJECT o)
    {
        boolean empty = (o.methods.size() == 0 && o.fields.size() == 0);
        out.print("OBJECT(" + o.myClass.name);
        if (!empty) {
            indentCount++;
            indent(); o.methods.accept(this);
            indent(); o.fields.accept(this);
            indentCount--;
        }
        out.print(")");
    }
 
    public void visit(INT i) { out.print("INT"); }
    public void visit(BOOLEAN b) { out.print("BOOLEAN"); }
    public void visit(VOID v) { out.print("VOID"); }
    public void visit(NIL n) { out.print("NIL"); }
    public void visit(STRING s) { out.print("STRING"); }
}
