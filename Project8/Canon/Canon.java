/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Canon;
import java.util.LinkedList;

class MoveCall extends Tree.Stm 
{
    Tree.TEMP dst;
    Tree.CALL src;
    MoveCall(Tree.TEMP d, Tree.CALL s) 
	{
		dst = d; src = s;
    }
    public LinkedList<Tree.Exp> kids() 
	{
		return src.kids();
    }
    public Tree.Stm build(LinkedList<Tree.Exp> kids) 
	{
		return new Tree.MOVE(dst, src.build(kids));
    }
    public void accept(Tree.IntVisitor v, int i) { throw new Error(); }
    public void accept(Tree.CodeVisitor v)       { throw new Error(); }
}

class ExpCall extends Tree.Stm 
{
    Tree.CALL call;
    ExpCall(Tree.CALL c) 
	{
		call = c;
    }

    public LinkedList<Tree.Exp> kids() 
	{
		return call.kids();
    }

    public Tree.Stm build(LinkedList<Tree.Exp> kids) 
	{
		return new Tree.EXP(call.build(kids));
    }

    public void accept(Tree.IntVisitor v, int i) { throw new Error(); }
    public void accept(Tree.CodeVisitor v)       { throw new Error(); }
}

class StmExpList 
{
    Tree.Stm stm;
    LinkedList<Tree.Exp> exps;
    StmExpList(Tree.Stm s, LinkedList<Tree.Exp> e) 
	{
		stm = s;
		exps = e;
    }
}

/**
 * Implements the transformation into canonical trees.
 */

public class Canon 
{
    static boolean isNop(Tree.Stm a) 
	{
		return a instanceof Tree.EXP
			&& ((Tree.EXP)a).exp instanceof Tree.CONST;
    }

    static Tree.Stm seq(Tree.Stm a, Tree.Stm b) 
	{
		if (isNop(a))
			return b;
		else if (isNop(b))
			return a;
		else
			return new Tree.SEQ(a, b);
    }

    static boolean commute(Tree.Stm a, Tree.Exp b) 
	{
		return isNop(a) || b instanceof Tree.NAME || b instanceof Tree.CONST;
    }

    static Tree.Stm do_stm(Tree.SEQ s) 
	{ 
		return seq(do_stm(s.left), do_stm(s.right));
    }
	
    static Tree.Stm do_stm(Tree.MOVE s) 
	{ 
		if (s.dst instanceof Tree.TEMP && s.src instanceof Tree.CALL) 
			return reorder_stm(new MoveCall((Tree.TEMP)s.dst,
											(Tree.CALL)s.src));
		else if (s.dst instanceof Tree.ESEQ)
			return do_stm(new Tree.SEQ(((Tree.ESEQ)s.dst).stm,
									   new Tree.MOVE(((Tree.ESEQ)s.dst).exp,
													 s.src)));
		else
			return reorder_stm(s);
    }

    static Tree.Stm do_stm(Tree.EXP s) 
	{ 
		if (s.exp instanceof Tree.CALL)
			return reorder_stm(new ExpCall((Tree.CALL)s.exp));
		else
			return reorder_stm(s);
    }

    static Tree.Stm do_stm(Tree.Stm s) 
	{
		if (s instanceof Tree.SEQ)
			return do_stm((Tree.SEQ)s);
		else if (s instanceof Tree.MOVE)
			return do_stm((Tree.MOVE)s);
		else if (s instanceof Tree.EXP)
			return do_stm((Tree.EXP)s);
		else
			return reorder_stm(s);
    }

    static Tree.Stm reorder_stm(Tree.Stm s) 
	{
		StmExpList x = reorder(s.kids());
		return seq(x.stm, s.build(x.exps));
    }

    static Tree.ESEQ do_exp(Tree.ESEQ e) 
	{
		Tree.Stm stms = do_stm(e.stm);
		Tree.Exp a = e.exp;
		Tree.ESEQ b = do_exp(e.exp);
		return new Tree.ESEQ(seq(stms, b.stm), b.exp);
    }

    static Tree.ESEQ do_exp (Tree.CALL e) 
	{
		Temp.Temp t = new Temp.Temp();
		Tree.Exp a = new Tree.ESEQ(new Tree.MOVE(new Tree.TEMP(t), e),
								   new Tree.TEMP(t));
		return do_exp(a);
    }

    static Tree.ESEQ do_exp (Tree.Exp e) 
	{
		if (e instanceof Tree.ESEQ)
			return do_exp((Tree.ESEQ)e);
		else if (e instanceof Tree.CALL)
			return do_exp((Tree.CALL)e);
		else
			return reorder_exp(e);
    }

    static Tree.ESEQ reorder_exp (Tree.Exp e) 
	{
		StmExpList x = reorder(e.kids());
		return new Tree.ESEQ(x.stm, e.build(x.exps));
    }

    static final Tree.Stm nopNull = new Tree.EXP(new Tree.CONST(0));

    static StmExpList reorder(LinkedList<Tree.Exp> exps) 
	{
		if (exps.isEmpty())
			return new StmExpList(nopNull, exps);
		else 
		{
			Tree.Exp a = exps.removeFirst();
			Tree.ESEQ aa = do_exp(a);
			StmExpList bb = reorder(exps);
			if (commute(bb.stm, aa.exp)) 
			{
				bb.exps.addFirst(aa.exp);
				return new StmExpList(seq(aa.stm, bb.stm), bb.exps);
			} else {
				Temp.Temp t = new Temp.Temp();
				bb.exps.addFirst(new Tree.TEMP(t));
				return
					new StmExpList(seq(aa.stm,
									   seq(new Tree.MOVE(new Tree.TEMP(t),
														 aa.exp),
										   bb.stm)),
								   bb.exps);
			}
		}
    }
	
    static LinkedList<Tree.Stm> linear(Tree.SEQ s, LinkedList<Tree.Stm> l) 
	{
		return linear(s.left, linear(s.right, l));
    }

    static LinkedList<Tree.Stm> linear(Tree.Stm s, LinkedList<Tree.Stm> l) 
	{
		if (s instanceof Tree.SEQ)
			return linear((Tree.SEQ)s, l);
		else 
		{
			l.addFirst(s);
			return l;
		}
    }
	
    static public LinkedList<Tree.Stm> linearize(Tree.Stm s) 
	{
		return linear(do_stm(s), new LinkedList<Tree.Stm>());
    }
}
