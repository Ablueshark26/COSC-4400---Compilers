/**
 * COSC 4400 - Project #7
 * 
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
package Translate;
import Temp.Label;

public class Ex extends Exp {
    Tree.Exp exp;
    Ex(Tree.Exp e)  { exp = e; }
    Tree.Exp unEx() { return exp; }

    Tree.Stm unNx() {
        if (exp instanceof Tree.CONST) return null;
        return new Tree.EXP(exp);
    }

    Tree.Stm unCx(Label t, Label f) {
        if (exp instanceof Tree.CONST) {
            int v = ((Tree.CONST) exp).value;
            return new Tree.JUMP(v == 0 ? f : t);
        }
        return new Tree.CJUMP(Tree.CJUMP.NE, exp, new Tree.CONST(0), t, f);
    }
}
