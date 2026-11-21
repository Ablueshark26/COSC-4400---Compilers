/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Canon;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

/**
 * Arranges the blocks so that CJUMP is followed by its false label.
 */

public class TraceSchedule
{
    Temp.Label done;
    Iterator<LinkedList<Tree.Stm>> theBlocks;
    List<Tree.Stm> stms;
    HashMap<Temp.Label,LinkedList<Tree.Stm>> map
		= new HashMap<Temp.Label,LinkedList<Tree.Stm>>();

    Tree.Stm getLast(LinkedList<Tree.Stm> block) 
	{
		Tree.Stm last = block.removeLast();
		stms.addAll(block);
		return last;
    }

    void trace(LinkedList<Tree.Stm> slist)
	{
		for(;;) 
		{
			Tree.Stm s = slist.getFirst();
			Tree.LABEL lab = (Tree.LABEL)s;
			map.remove(lab.label);
			s = getLast(slist);
			if (s instanceof Tree.JUMP) 
			{
				Tree.JUMP j = (Tree.JUMP)s;
				Temp.Label first = j.targets.getFirst();
				LinkedList<Tree.Stm> target = map.get(first);
				int size = j.targets.size();
				if (size == 1 && target != null) 
				{
					slist = target;
				} 
				else 
				{
					if (theBlocks.hasNext()
						|| first != done
						|| size > 1)
						stms.add(s);
					return;
				}
			} 
			else if (s instanceof Tree.CJUMP) 
			{
				Tree.CJUMP j = (Tree.CJUMP)s;
				LinkedList<Tree.Stm> t = map.get(j.iftrue);
				LinkedList<Tree.Stm> f = map.get(j.iffalse);
				if (f != null) 
				{
					stms.add(s);
					slist = f;
				} 
				else if (t != null) 
				{
					stms.add(new Tree.CJUMP(Tree.CJUMP.notRel(j.relop),
											j.left, j.right,
											j.iffalse, j.iftrue));
					slist = t;
				} 
				else 
				{
					Temp.Label ff = new Temp.Label();
					stms.add(new Tree.CJUMP(j.relop, j.left, j.right,
											j.iftrue, ff));
					stms.add(new Tree.LABEL(ff));
					stms.add(new Tree.JUMP(j.iffalse));
					return;
				}
			} 
			else
				throw new Error("Bad basic block in TraceSchedule");
		}
    }

    public TraceSchedule(BasicBlocks b, List<Tree.Stm> stmts) 
	{
		for (LinkedList<Tree.Stm> s : b.blocks)
		{
			map.put(((Tree.LABEL)s.getFirst()).label, s);
		}
		done = b.done;
		theBlocks = b.blocks.iterator();
		stms = stmts;
		while (theBlocks.hasNext())
		{
			LinkedList<Tree.Stm> s = theBlocks.next();
			Tree.LABEL lab = (Tree.LABEL)s.getFirst();
			if (map.get(lab.label) != null)
				trace(s);
		}
		stms.add(new Tree.LABEL(done));
		map = null;
    }
}
