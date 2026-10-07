/**
 * @authors Luke Sam
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
package Semant;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import Absyn.Program;

public class Main
{
    public static void main(String[] args)
    {
        InputStream in = System.in;
        try
        {
            if (args.length > 0)
                in = new FileInputStream(args[0]);
        }
        catch (IOException e)
        {
            System.err.println("Cannot open " + args[0] + ": " + e.getMessage());
            System.exit(1);
        }
        //find way to feed absyn to type checker
    }
}
