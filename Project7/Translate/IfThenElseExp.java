/**
 * COSC 4400 - Project #7
 * IfThenElseExp: if-then-else that may be used as an expression, a statement or a condition.
 * @authors Luke Sam
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
package Translate;
import Temp.Temp;
import Temp.Label;

public class IfThenElseExp extends Exp {
    Exp cond, trueExp, falseExp;

    IfThenElseExp(Exp cExp, Exp tExp, Exp fExp) {
	cond = cExp;
	trueExp = tExp;
	falseExp = fExp;
    }

    private static Tree.Stm SEQ(Tree.Stm l, Tree.Stm r) {
	if (l == null) return r;
	if (r == null) return l;
	return new Tree.SEQ(l, r);
    }
    private static Tree.Stm LABEL(Label l) { return new Tree.LABEL(l); }
    private static Tree.Stm JUMP(Label l)  { return new Tree.JUMP(l); }

    /** Used as a condition: each branch jumps straight to the final targets. */
    Tree.Stm unCx(Label tt, Label ff) {
	Label t = new Label();
	Label f = new Label();
	return SEQ(cond.unCx(t, f),
		   SEQ(SEQ(LABEL(t), trueExp.unCx(tt, ff)),
		       SEQ(LABEL(f), falseExp.unCx(tt, ff))));
    }

    /** Used as a value: both arms leave their result in a fresh temp. */
    Tree.Exp unEx() {
	Tree.Exp tExp = trueExp.unEx();
	Tree.Exp fExp = falseExp.unEx();
	Temp r = new Temp();
	Label t = new Label();
	Label f = new Label();
	Label join = new Label();
	return new Tree.ESEQ(
	    SEQ(SEQ(cond.unCx(t, f),
		    SEQ(SEQ(LABEL(t),
			    SEQ(new Tree.MOVE(new Tree.TEMP(r), tExp), JUMP(join))),
			SEQ(LABEL(f),
			    SEQ(new Tree.MOVE(new Tree.TEMP(r), fExp), JUMP(join))))),
		LABEL(join)),
	    new Tree.TEMP(r));
    }

    /** Used as a statement: run the chosen arm, then fall into the join label. */
    Tree.Stm unNx() {
	Tree.Stm tStm = trueExp.unNx();
	Tree.Stm fStm = falseExp.unNx();
	Label t = new Label();
	Label f = new Label();
	Label join = new Label();
	tStm = SEQ(SEQ(LABEL(t), tStm), JUMP(join));
	fStm = SEQ(SEQ(LABEL(f), fStm), JUMP(join));
	return SEQ(SEQ(cond.unCx(t, f), SEQ(tStm, fStm)), LABEL(join));
    }
}
