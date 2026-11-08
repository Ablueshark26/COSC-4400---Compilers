package Parse;
 /**
  2  * COSC 4400 - Project #3
  3  * 
  4  * @authors Luke Sam 
  5  * Instructor Dr. Brylow
  6  * TA-BOT:MAILTO luke.sharba@marquette.edu 
  7  * TA-BOT:MAILTO samuel.biskupic@marquette.edu
  8  */

import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Main
{   
    public static void main(String [] args) 
    {
	InputStreamReader isr =	new InputStreamReader(System.in);
        Reader reader = new BufferedReader(isr);

	try
	{
	Absyn.Program parse = new MiniJavaParser(reader).Goal();
	PrintWriter writer = new PrintWriter(System.out);
	Absyn.PrintVisitor pv =	new Absyn.PrintVisitor(writer);
	pv.visit(parse);
	writer.flush();
	}
    catch (ParseException p)
	{
	    System.out.println(p.toString());
	    System.exit(-1);
	}
    }
}
