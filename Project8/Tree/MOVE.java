/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Tree;
import Temp.Temp;
import Temp.Label;
import java.util.LinkedList;

/**
 * Implements a move instruction.
 */

public class MOVE extends Stm 
{
    public Exp dst, src;
    public MOVE(Exp d, Exp s) { dst=d; src=s; }
    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public void accept(CodeVisitor v) { v.visit(this); }
    public LinkedList<Exp> kids()
    {
        LinkedList<Exp> l = new LinkedList<Exp>();
        if (dst instanceof MEM) l.add(((MEM) dst).exp);
        l.add(src);
        return l;
    }
    public Stm build(LinkedList<Exp> kids)
    {
        if (dst instanceof MEM)
            return new MOVE(new MEM(kids.get(0)), kids.get(1));
        return new MOVE(dst, kids.get(0));
    }

}
