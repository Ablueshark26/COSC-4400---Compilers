/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Tree;
import Temp.Temp;
import Temp.Label;
import java.util.List;
import java.util.LinkedList;

/**
 * Implements a procedure call, application of a function to an
 * argument list.
 */

public class CALL extends Exp 
{
    public Exp func;
    public List<Exp> args;
    public CALL(Exp f, List<Exp> a) { func=f; args=a; }
    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public Temp accept(CodeVisitor v) { return v.visit(this); }
    public LinkedList<Exp> kids()
    {
        LinkedList<Exp> l = new LinkedList<Exp>();
        l.add(func);
        l.addAll(args);
        return l;
    }
    public Exp build(LinkedList<Exp> kids)
    {
        LinkedList<Exp> a = new LinkedList<Exp>(kids);
        Exp f = a.removeFirst();
        return new CALL(f, a);
    }
}
