/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Assem;
import Temp.Temp;
import Temp.Label;
import java.util.List;

/**
 * A point within the program to which a jump may go.
 */

public class LABEL extends Instr 
{
    public Label label;
	
    public LABEL(String assem, Label label) 
	{
		this.assem = assem;
		use = null;
		def = null;
		jumps = null;
		this.label = label;
    }

	public void output(java.io.PrintWriter writer)
	{
		writer.print("  LABEL(\"" + label + "\"");
		super.output(writer);
		writer.println(")");
	}
}
