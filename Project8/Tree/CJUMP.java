/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Tree;
import Temp.Temp;
import Temp.Label;
import java.util.LinkedList;

/**
 * Implements a conditional jump.
 */

public class CJUMP extends Stm 
{
    public int relop;
    public Exp left, right;
    public Label iftrue, iffalse;
    public CJUMP(int rel, Exp l, Exp r, Label t, Label f) 
	{
		relop=rel; left=l; right=r; iftrue=t; iffalse=f;
    }
    
    public static int notRel(int relop)
    {
        switch (relop)
        {
        case EQ:  return NE;
        case NE:  return EQ;
        case LT:  return GE;
        case GE:  return LT;
        case GT:  return LE;
        case LE:  return GT;
        case ULT: return UGE;
        case UGE: return ULT;
        case UGT: return ULE;
        case ULE: return UGT;
        default:  throw new Error("bad relop in CJUMP.notRel");
        }
    }

    public final static int EQ=0, NE=1, LT=2, GT=3, LE=4, GE=5,
		ULT=6, ULE=7, UGT=8, UGE=9;

    public void accept(IntVisitor v, int d) { v.visit(this, d); }
        public void accept(CodeVisitor v) { v.visit(this); }
    public LinkedList<Exp> kids()
    {
        LinkedList<Exp> l = new LinkedList<Exp>();
        l.add(left);
        l.add(right);
        return l;
    }
    public Stm build(LinkedList<Exp> kids)
    {
        return new CJUMP(relop, kids.get(0), kids.get(1), iftrue, iffalse);
    }
}
