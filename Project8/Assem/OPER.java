/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Assem;
import Temp.Temp;
import Temp.Label;
import java.util.List;

/**
 * Holds an assembly-instruction, a list of operand registers,
 *  and a list of result registers.
 */

public class OPER extends Instr 
{
    public OPER(String a, Temp[] d, Temp[] s, List<Label> j) 
	{
		assem = a;
		use = s;
		def = d;
		jumps = j;
    }

	public void output(java.io.PrintWriter writer)
	{
		writer.print("  OPER(\"" + assem + "\"");
		super.output(writer);
		writer.println(")");
	}
}
