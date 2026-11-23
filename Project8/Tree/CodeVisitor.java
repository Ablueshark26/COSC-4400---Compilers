/**
 * @authors Luke Sam
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
package Tree;
import Temp.Temp;

public interface CodeVisitor {
    // statements
    public void visit(SEQ n);
    public void visit(LABEL n);
    public void visit(JUMP n);
    public void visit(CJUMP n);
    public void visit(MOVE n);
    public void visit(EXP n);
    // expressions
    public Temp visit(BINOP n);
    public Temp visit(MEM n);
    public Temp visit(TEMP n);
    public Temp visit(ESEQ n);
    public Temp visit(NAME n);
    public Temp visit(CONST n);
    public Temp visit(CALL n);
}

