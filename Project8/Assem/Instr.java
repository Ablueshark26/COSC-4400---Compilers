/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Assem;
import Temp.Temp;
import Temp.Label;
import java.util.List;

/**
 * A data type used for assembly language without assigned registers.
 */

public abstract class Instr
{
    public String assem;
    public Temp[] use;
    public Temp[] def;
    public List<Label> jumps;

	public void output(java.io.PrintWriter writer)
	{
		if ((def != null) && (def.length > 0))
                {
                        writer.print(" defs(");
                        for (int i = 0; i< def.length; i++)
                        {   writer.print(def[i] + " ");   }
                        writer.print(")");
                }
		if ((use != null) && (use.length > 0))
		{
			writer.print(" uses(");
			for (int i = 0; i< use.length; i++)
			{   writer.print(use[i] + " ");   }
			writer.print(")");
		}
		if ((jumps != null) && (jumps.size() > 0))
		{
			writer.print(" jumps(");
			for (Label label : jumps)
			{   writer.print(label + " ");   }
			writer.print(")");
		}
	}

    /** Replaces the src list. */
    public void replaceUse(Temp olduse, Temp newuse) 
	{
		if (use != null)
			for (int i = 0; i< use.length; i++)
				if (use[i] == olduse) use[i] = newuse;
    }

    /** Replaces the dst list. */
    public void replaceDef(Temp olddef, Temp newdef) 
	{
		if (def != null)
			for (int i = 0; i< def.length; i++)
				if (def[i] == olddef) def[i] = newdef;
    };

    /** Formats an assembly instruction as a string. */
    public String toString() 
	{
		StringBuffer s = new StringBuffer();
		int len = assem.length();
		for(int i=0; i<len; i++)
			if (assem.charAt(i)=='`')
				switch(assem.charAt(++i)) 
				{
				case 's': 
				{
					int n = Character.digit(assem.charAt(++i),10);
					s.append(use[n].toString());
					break;
				}
				case 'd': 
				{
					int n = Character.digit(assem.charAt(++i),10);
					s.append(def[n].toString());
					break;
				}
				case 'j': 
				{
					int n = Character.digit(assem.charAt(++i),10);
					s.append(jumps.get(n).toString());
					break;
				}
				case '`':
					s.append('`'); 
					break;
				default:
					throw new Error("bad Assem format:" + assem);
				}
			else s.append(assem.charAt(i));
		return s.toString();
    }
}
