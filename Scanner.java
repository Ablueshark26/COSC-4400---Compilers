/**
 * COSC 4400 - Project #1
 * Explain briefly the functionality of the program.
 * @authors Luke Sam 
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu 
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
import java.io.BufferedReader;                  
import java.io.InputStreamReader;      

public class Scanner{

	public final boolean Debug = false;	
		
	public enum CharType
	{ LETTER, DIGIT, OPERATORS, PUNC, QUOTE, COMMENT, OTHER};
      	
	public CharType characterClass[] = new CharType[256];

	public enum State
	{ START, ALPHA-BUILDING, ALPHA-ACCEPT, QUOTE-BUILDING, QUOTE-ACCEPT, INT-BUILDING, INT-ACCEPT, HEX-BUILDING, HEX-ACCEPT, OCT-BUILDING, OCT-ACCEPT, OP-BUILDING, OP-ACCEPT, COMMENT-BUILDING, SINGLE-COMMENT, MULTI-COMMENT, COMMENT-ACCPET, ERR };

	public State next_state[][] = //temp 2d array while figuring out input
	{ {State.BUILDING, State.ERR, State.ERR},
      {State.BUILDING, State.BUILDING, State.ACCEPT},
      {State.ACCEPT, State.ACCEPT, State.ACCEPT},
      {State.ERR, State.ERR, State.ERR}};

	public Scanner()
	{
		for(int i = 0; i < characterClass.length; i++)
			characterClass[i] = CharType.OTHER;
		for(int i = 'A'; i <= 'Z'; i++)
			characterClass[i] = CharType.LETTER;
		for(int i = 'a'; i <= 'z'; i++)
			characterClass[i] = CharType.LETTER;
		for(int i = '0'; i <= '9'; i++)
                        characterClass[i] = CharType.DIGIT;
                characterClass[38] = CharType.OPERATORS; //adding &
		characterClass[124] = CharType.OPERATORS; //adding |
		characterClass[94] = CharType.OPERATORS; //adding ^
		characterClass[126] = CharType.OPERATORS; //adding ~
		characterClass[43] = CharType.OPERATORS; //adding +
		characterClass[45] = CharType.OPERATORS; //adding -
		characterClass[42] = CharType.OPERATORS; //adding *
		characterClass[47] = CharType.OPERATORS; //adding /
		characterClass[60] = CharType.OPERATORS; //adding <
		characterClass[62] = CharType.OPERATORS; //adding >
		characterClass[61] = CharType.OPERATORS; //adding =
		characterClass[33] = CharType.OPERATORS; //adding !
		characterClass[40] = CharType.PUNC; //adding (
		characterClass[41] = CharType.PUNC; //adding )
		characterClass[91] = CharType.PUNC; //adding [  
		characterClass[93] = CharType.PUNC; //adding ]
		characterClass[123] = CharType.PUNC; //adding {
		characterClass[125] = CharType.PUNC; //adding }
	}
	public ArrayList<String> restricted = new ArrayList<String>(
		Arrays.asList("int", "boolean", "String", "char"));


public String getToken (java.io.Reader reader) throws java.io.IOException
{
	State state = State.START;
	int c = reader.read();

	String lexeme = "";

	while (-1 != c)
	{
		CharType charClass = characterClass[c];

		if (Debug) System.out.print ("state = " + state + ", class = " + charClass);
		state = next_state[state.ordinal() ][charClass.ordinal() ];
		if (Debug) System.out.println (" ==> state = " + state);
		switch (state)
		{
			case BUILDING:
				lexeme = lexeme + (char) c;
				if (Debug) System.out.println("Currentl reading" + c);
				c = reader.read();
				break;
			case ACCEPT:
				return "ID(" + lexeme + ")";
			case ERR:
				//Temp message will need to put out required Error
				return "ERROR_TOKEN";
			default:
				System.err.println("ERROR: Reached wrong state " + state);
				return "ERROR_TOKEN";
		}
	}
	return "EOF";
}
}


public static void main(String[] args) throws java.io.IOException
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

