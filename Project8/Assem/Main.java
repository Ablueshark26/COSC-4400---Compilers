/* Copyright (C) 2009, Dennis Brylow
 * All rights reserved.  */

package Assem;
import java.io.PrintWriter;
import java.util.Iterator;

import Parse.*;

import java.util.HashMap; 
import java.util.List;
import java.util.LinkedList;

public class Main 
{
    static final String VERSION = "COSC 170" +
		" Instruction Selection" +
		" Spring 2009" +
		" v1.1";

	public static void usage()
	{
		System.err.println("Usage:  [-options] (file.java | -)");
		System.err.println("\twhere options include:");
		System.err.println("\t-a\tOutput Assembly stage");
		System.err.println("\t-b\tOutput Basic Block stage");
		System.err.println("\t-c\tOutput Canonical Linearization stage");
		System.err.println("\t-p\tOutput Prologue / Epilogue stage");
		System.err.println("\t-t\tOutput Trace Scheduling stage");
		System.err.println("\t-v\tPrint version and exit");
		System.exit(-2);
	}

    public static void main(String args[])
    {
		boolean assemOutput = false,
			canonOutput = false,
			blockOutput = false,
			traceOutput = false,
			prologOutput = false;
		java.io.Reader reader = null;
		
		for (int i = 0; i < args.length; i++)
	    {
			if (args[i].equals("-v"))
		    {
				System.out.println(VERSION);
				System.exit(0);
		    }
			else if (args[i].equals("-a"))
			{   assemOutput = true;   }
			else if (args[i].equals("-b"))
			{   blockOutput = true;   }
			else if (args[i].equals("-c"))
			{   canonOutput = true;   }
			else if (args[i].equals("-p"))
			{   prologOutput = true;   }
			else if (args[i].equals("-t"))
			{   traceOutput = true;   }
			else if (args[i].equals("-"))
		    {
				java.io.InputStreamReader isr = 
					new java.io.InputStreamReader(System.in);
				reader = new java.io.BufferedReader(isr);
		    }
			else
		    {
				try
			    {
					reader = new java.io.FileReader(args[i]);
			    }
				catch (java.io.FileNotFoundException fnfe)
			    {
					System.err.println("File Not Found: " + args[i]);
					System.exit(-1);
			    }
		    }
	    }
		
		if (null == reader) usage();
		
		LinkedList<Translate.Frag> frags = null;

		try
	    {
			frags = new Parse.ReadFrags(reader).Program();
	    }
		catch (ParseException p)
	    {
			System.err.println(p.toString());
			System.exit(-1);
	    }

		PrintWriter writer = new PrintWriter(System.out);
		
		for (Translate.Frag frag : frags)
		{
			if (frag instanceof Translate.DataFrag) 
			{
				writer.println("DataFrag(");
				writer.println(frag);
				writer.println(")");
			} 
			else 
			{
				Translate.ProcFrag p = (Translate.ProcFrag)frag;
				List<Tree.Stm> traced = new LinkedList<Tree.Stm>();
				if (p.body != null) 
				{
					LinkedList<Tree.Stm> stms = Canon.Canon.linearize(p.body);
					if (canonOutput)
					{
						writer.println("/* **** CANONICAL TREES **** */");
						for (Tree.Stm s : stms)
						{
							new Tree.Print(writer, s);
							writer.println();
						}
						writer.flush();
					}
					Canon.BasicBlocks blocks = new Canon.BasicBlocks(stms);
					if (blockOutput)
					{
						writer.println("/* **** BASIC BLOCKS **** */");
						for (LinkedList<Tree.Stm> slist : blocks.blocks)
						{
							for (Tree.Stm s : slist)
							{
								new Tree.Print(writer, s);
								writer.println();
							}
						}
						new Tree.Print(writer, new Tree.LABEL(blocks.done));
						writer.flush();
					}
					new Canon.TraceSchedule(blocks, traced);
					if (traceOutput)
					{
						writer.println("/* **** TRACE SCHEDULED **** */");
						for (Tree.Stm s : traced)
						{
							new Tree.Print(writer, s);
							writer.println();
						}
						writer.flush();						
					}
				}
				p.frame.procEntryExit1(traced);
				if (prologOutput)
				{
					writer.println("/* **** PROLOGUE / EPILOGUE **** */");
					for (Tree.Stm s : traced)
					{
						new Tree.Print(writer, s);
						writer.println();
					}
					writer.flush();
				}

				List<Assem.Instr> code = p.frame.codeGen(traced);
				if (assemOutput)
				{
					for (Assem.Instr i : code)
					{
						writer.println(i);
					}
				}

				writer.println("ProcFrag(");
				p.frame.printFrame(writer);
				for (Assem.Instr i : code)
				{
					i.output(writer);
				}
				writer.println(")");
			}
		}  
		writer.flush();
	}
}	

