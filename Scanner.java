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
	{ START, ALPHA_BUILDING, ALPHA_ACCEPT, QUOTE_BUILDING, QUOTE_ACCEPT, INT_BUILDING, INT_ACCEPT, HEX_BUILDING, HEX_ACCEPT, OCT_BUILDING, OCT_ACCEPT, OP_BUILDING, OP_ACCEPT, COMMENT_BUILDING, SINGLE_COMMENT, MULTI_COMMENT, COMMENT_ACCPET, ERR };

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
			case ALPHA_BUILDING:
				lexeme = lexeme + (char) c;
				if (Debug) System.out.println("Currentl reading" + c);
				c = reader.read();
				break;
			case ALPHA_ACCEPT:
				return "ID(" + lexeme + ")";
			case QUOTE_BUILDING:
				lexeme = lexeme + (char) c;
				if (Debug) System.out.println("Currentl reading" + c);
				c = reader.read();
				break;
			case QUOTE_ACCEPT:
				return "STRING_LITERAL(" + lexeme + ")";
			case INT_BUILDING:
				lexeme = lexeme + (char) c;
 				if (Debug) System.out.println("Currentl reading" + c);
				c = reader.read();
				break;
			case INT_ACCEPT:
				return "INTEGER_LITERAL(" + lexeme + ")";
			case HEX_BUILDING:
				lexeme = lexeme + (char) c;
				if (Debug) System.out.println("Currentl reading" + c);
				c = reader.read();
				break;
			case HEX_ACCEPT:
				return "HEXADECIMAL_LITERAL(" + lexeme + ")";
			case OCT_BUILDING:
				lexeme = lexeme + (char) c;
				if (Debug) System.out.println("Currentl reading" + c);
				c = reader.read();
				break;
			case OCT_ACCEPT:
				return "OCTAL_LITERAL(" + lexeme + ")";
			case OP_BUILDING:
				lexeme = lexeme + (char) c;
				if (Debug) System.out.println("Currentl reading" + c);
				c = reader.read();
				break;
			case OP_ACCEPT:
				if(lexeme == "&&"){
					return "AND";
				}
				else if (lexeme == "||"){
					return "OR";
				}
				else if (lexeme == "&"){
					return "BWAND";
				}
				else if (lexeme == "|"){
					return "BWOR";
				}
				else if (lexeme == "^"){
					return "XOR";
				}
				else if (lexeme == "~"){
					return "COMP";
				}
				else if (lexeme == "+"){
					return "PLUS";
				}
				else if (lexeme == "-"){
					return "MINUS";
				}
				else if (lexeme == "*"){
					return "STAR";
				}
				else if (lexeme == "/"){
					return "FORWARDSLASH";
				}
				else if (lexeme == "<"){
					return "LESSTHAN";
				}
				else if (lexeme == ">"){
					return "GREATERTHAN";
				}
				else if (lexeme == "=="){
					return "EQUAL";
				}
				else if (lexeme == "!="){
					return "NOTEQUAL";
				}
				else if (lexeme == "!"){
					return "BANG";
				}
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
        Scanner s = new Scanner();

        reader = new java.io.BufferedReader (new java.io.InputStreamReader (System.in));

        String token;
        do
        {
            token = s.getToken (reader);
            System.out.println (token);
        }
        while (!token.equals ("EOF"));
}

