/**
 * COSC 4400 - Project #7
 * Ex: translation-phase expression that yields a value (wraps a Tree.Exp).
 * @authors Luke Sam
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

    /** Value is discarded: constants vanish, anything else is evaluated for effect. */
    Tree.Stm unNx() {
        if (exp instanceof Tree.CONST) return null;
        return new Tree.EXP(exp);
    }

    /** Constants become unconditional jumps; otherwise test exp != false (0). */
    Tree.Stm unCx(Label t, Label f) {
        if (exp instanceof Tree.CONST) {
            int v = ((Tree.CONST) exp).value;
            return new Tree.JUMP(v == 0 ? f : t);
        }
        return new Tree.CJUMP(Tree.CJUMP.NE, exp, new Tree.CONST(0), t, f);
    }
}
