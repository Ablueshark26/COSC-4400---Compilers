/**
 * @authors Luke Sam
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
package Semant;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
 
public class Main
{
    public static void main(String[] args)
    {
        InputStreamReader isr = new InputStreamReader(System.in);
        Reader reader = new BufferedReader(isr);
 
        try
        {
            Absyn.Program prog = new ReadAbsyn(reader).Program();
            TypeChecker tc = new TypeChecker();
            tc.visit(prog);
            PrintWriter pw = new PrintWriter(System.out);
            tc.printClasses(pw);
        }
        catch (ParseException p)
        {
            System.out.println(p.toString());
            System.exit(-1);
        }
    }
} 
