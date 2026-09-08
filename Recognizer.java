import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Recognizer
{

    public final boolean DEBUG = true;

    public enum CharType
    { LETTER, DIGIT, OTHER };

    public CharType characterClass[] = new CharType[256];

    public enum State
    { START, BUILDING, ACCEPT, ERR };

    public State next_state[][] =
    { {State.BUILDING, State.ERR, State.ERR},
      {State.BUILDING, State.BUILDING, State.ACCEPT},
      {State.ACCEPT, State.ACCEPT, State.ACCEPT},
      {State.ERR, State.ERR, State.ERR}
    };

    public Recognizer ()
    {
        for (int i = 0; i < characterClass.length; i++)
            characterClass[i] = CharType.OTHER;
        for (int i = 'A'; i <= 'Z'; i++)
            characterClass[i] = CharType.LETTER;
        for (int i = 'a'; i <= 'z'; i++)
            characterClass[i] = CharType.LETTER;
        for (int i = '0'; i <= '9'; i++)
            characterClass[i] = CharType.DIGIT;
    }

    public String getToken (java.io.Reader reader) throws java.io.IOException
    {
        State state = State.START;
        int c = reader.read ();
        String lexeme = "";

        while (-1 != c)
        {
            CharType charClass = characterClass[c];
            if (DEBUG) System.out.print ("state = " + state + ", class = " + charClass);
            state = next_state[state.ordinal ()][charClass.ordinal ()];
            if (DEBUG) System.out.println (" ==> state = " + state);
            switch (state)
            {
            case BUILDING:
                lexeme = lexeme + (char) c;
                c = reader.read ();
                break;
            case ACCEPT:return "ID(" + lexeme + ")";
            case ERR:return "ERROR_TOKEN";
            default:System.err.println ("ERROR: Reached wrong state " + state);
                return "ERROR_TOKEN";
            }
        }
        return "EOF";
    }

    public static void main (String[]args) throws java.io.IOException
    {
        java.io.Reader reader = null;
        Recognizer r = new Recognizer ();

        reader =
            new java.io.BufferedReader (new java.io.InputStreamReader (System.in));

        String token;
        do
        {
            token = r.getToken (reader);
            System.out.println (token);
  	}
        while (!token.equals ("EOF"));
    }
}
