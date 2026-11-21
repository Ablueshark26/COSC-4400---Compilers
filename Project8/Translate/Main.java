/**
 * COSC 4400 - Project #7
 * @authors Luke Sam
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
package Translate;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class Main
{
    private static String slurp(InputStream in) throws IOException
    {
	ByteArrayOutputStream buf = new ByteArrayOutputStream();
	byte[] chunk = new byte[4096];
	int n;
	while ((n = in.read(chunk)) > 0) buf.write(chunk, 0, n);
	return buf.toString("UTF-8");
    }

    public static void main(String args[])
    {
	String text = null;
	try
	{
	    if (args.length > 0) text = slurp(new FileInputStream(args[0]));
	    else                 text = slurp(System.in);
	}
	catch (IOException e)
	{
	    System.err.println("Cannot read input: " + e.getMessage());
	    System.exit(-1);
	}

	if (text.trim().startsWith("ERROR"))
	{
	    System.err.println("Type checker reported errors; nothing to translate:");
	    System.err.println(text.trim());
	    System.exit(-1);
	}

	Absyn.Program program = null;
	try
	{
	    if (text.trim().startsWith("Program"))
		program = new ReadTypes(new StringReader(text)).Program();
	    else
		program = new Parse.MiniJavaParser(new StringReader(text)).Goal();
	}
	catch (ParseException p)
	{
	    System.err.println(p.toString());
	    System.exit(-1);
	}
	catch (Parse.ParseException p)
	{
	    System.err.println(p.toString());
	    System.exit(-1);
	}

	Translate translate = new Translate(new Riscv.RiscvFrame());
	program.accept(translate);

	PrintWriter writer = new PrintWriter(System.out);

	for (Iterator<Frag> frags = translate.results().iterator(); frags.hasNext(); )
	{
	    Frag f = frags.next();
	    if (f instanceof DataFrag)
	    {
		writer.println("DataFrag(");
		writer.println(f);
		writer.println(")");
	    }
	    else
	    {
		writer.println("ProcFrag(");
		ProcFrag p = (ProcFrag) f;
		p.frame.printFrame(writer);
		if (p.body != null)
		{
		    new Tree.Print(writer, p.body);
		    writer.println();
		}
		writer.println(")");
	    }
	}
	writer.flush();
    }
}
