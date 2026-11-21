package Parse;

/** Thrown by ReadFrags when the translator output cannot be understood. */
public class FragParseException extends Exception
{
    public FragParseException(String msg) { super(msg); }
}
