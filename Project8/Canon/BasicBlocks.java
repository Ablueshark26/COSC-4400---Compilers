/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Canon;
import java.util.List;
import java.util.LinkedList;

/**
 * Groups statements into sequences of straight-line code.
 */

public class BasicBlocks 
{
    public List<LinkedList<Tree.Stm>> blocks;
    public Temp.Label done;
    private LinkedList<Tree.Stm> lastStm;

    private void addStm(Tree.Stm s) 
	{
		lastStm.add(s);
    }

    private void doStms(LinkedList<Tree.Stm> slist) 
	{
		if (slist.isEmpty()) 
		{
			slist.add(new Tree.JUMP(done));
			doStms(slist);
		}
		else 
		{
			Tree.Stm s = slist.removeFirst();
			if (s instanceof Tree.JUMP || s instanceof Tree.CJUMP) 
			{
				addStm(s);
				mkBlocks(slist);
			} 
			else if (s instanceof Tree.LABEL) 
			{
				slist.addFirst(s);
				slist.addFirst(new Tree.JUMP(((Tree.LABEL)s).label));
				doStms(slist);
			}
			else 
			{
				addStm(s);
				doStms(slist);
			}
		}
    }

    void mkBlocks(LinkedList<Tree.Stm> slist) 
	{
		if (slist.isEmpty()) return;
		else 
		{
			Tree.Stm s = slist.removeFirst();
			if (s instanceof Tree.LABEL)
			{
				lastStm = new LinkedList<Tree.Stm>();
				lastStm.add(s);
				blocks.add(lastStm);
				doStms(slist);
			}
			else
			{
				slist.addFirst(s);
				slist.addFirst(new Tree.LABEL(new Temp.Label()));
				mkBlocks(slist);
			}
		}
    }

    public BasicBlocks(LinkedList<Tree.Stm> stms)
	{
		done = new Temp.Label();
		blocks = new LinkedList<LinkedList<Tree.Stm>>();
		mkBlocks(stms);
    }
}
