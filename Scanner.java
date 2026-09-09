/**
 * COSC 4400 - Project #1
 * This program reads an input line of characters and outputs the tokens in order
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
	{ LETTER_HEX, LETTER_X, LETTERS, DIGIT_ZERO, DIGIT_OCT, DIGITS, OP_STAR, OP_OR, OP_AND, OP_EQUAL, OP_EXC, OPERATORS, PUNC, QUOTE, COMMENT, SPACE, NEWLINE, OTHER};
      	
	public CharType characterClass[] = new CharType[256];

	public enum State
	{ START, ALPHA_BUILDING, ALPHA_ACCEPT, QUOTE_BUILDING, QUOTE_ACCEPT, QUOTE_ERR, ZERO, INT_BUILDING, INT_ACCEPT, HEX_BUILDING, HEX_ACCEPT, OCT_BUILDING, OCT_ACCEPT, OP_BUILDING, OP_ACCEPT, PUNC_ACCEPT, COMMENT_BUILDING, SINGLE_COMMENT, MULTI_COMMENT, COMMENT_ACCEPT, ERR };

	public State next_state[][] = new State[State.values().length][CharType.values().length];//Array will be size of states by chartypes

	public Scanner()
	{
		for(int i = 0; i < characterClass.length; i++)
			characterClass[i] = CharType.OTHER;
		for(int i = 'A'; i <= 'Z'; i++)
			characterClass[i] = CharType.LETTERS;
		for(int i = 'a'; i <= 'z'; i++)
			characterClass[i] = CharType.LETTERS;
		for(int i = 'A'; i <= 'F'; i++)
                        characterClass[i] = CharType.LETTER_HEX;
                for(int i = 'a'; i <= 'f'; i++)
                        characterClass[i] = CharType.LETTER_HEX;	
		characterClass['x'] = CharType.LETTER_X;
                characterClass['X'] = CharType.LETTER_X;

		for(int i = '1'; i <= '7'; i++)
                        characterClass[i] = CharType.DIGIT_OCT;
		characterClass['0'] = CharType.DIGIT_ZERO;
                characterClass['8'] = CharType.DIGITS;
                characterClass['9'] = CharType.DIGITS;
		

		characterClass['&'] = CharType.OP_AND;
		characterClass['|'] = CharType.OP_OR;
		characterClass['/'] = CharType.COMMENT;
		characterClass['*'] = CharType.OP_STAR;
		characterClass['='] = CharType.OP_EQUAL;
		characterClass['!'] = CharType.OP_EXC;
		characterClass['"'] = CharType.QUOTE;
		characterClass['^'] = CharType.OPERATORS;
		characterClass['~'] = CharType.OPERATORS;
		characterClass['+'] = CharType.OPERATORS;
		characterClass['-'] = CharType.OPERATORS;
		characterClass['<'] = CharType.OPERATORS;
		characterClass['>'] = CharType.OPERATORS;
		characterClass['('] = CharType.PUNC;
        	characterClass[')'] = CharType.PUNC;
        	characterClass['['] = CharType.PUNC;
        	characterClass[']'] = CharType.PUNC;
        	characterClass['{'] = CharType.PUNC;
        	characterClass['}'] = CharType.PUNC;
        	characterClass[','] = CharType.PUNC;
        	characterClass['.'] = CharType.PUNC;
        	characterClass[';'] = CharType.PUNC;
                characterClass[' '] = CharType.SPACE;
                characterClass['\t'] = CharType.SPACE;
                characterClass['\n'] = CharType.NEWLINE;



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
				else if (lexeme == "("){
					return "LPAREN";
				}
				else if (lexeme == ")"){
					return "RPAREN";
				}
				else if (lexeme == "["){
					return "LSQUARE";
				}
				else if (lexeme == "]"){
					return "RSQUARE";
				}
				else if (lexeme == "{"){
					return "LBRACE";
				}
				else if (lexeme == "}"){
					return "RBRACE";
				}
				else if (lexeme == ","){
					return "COMMA";
				}
				else if (lexeme == "."){
					return "PERIOD";
				}
				else if (lexeme == ";"){
					return "SEMICOLON";
				}
				else if (lexeme.toLowerCase() == "class"){
					return "CLASS";
				}
				else if (lexeme.toLowerCase() == "public"){
					return "PUBLIC";
				}
				else if (lexeme.toLowerCase() == "stadic"){
					return "STADIC";
				}
				else if (lexeme.toLowerCase() == "void"){
					return "VOID";
				}
				else if (lexeme.toLowerCase() == "main"){
					return "MAIN";
				}
				else if (lexeme.toLowerCase() == "string"){
					return "STRING";
				}
				else if (lexeme.toLowerCase() == "extends"){
					return "EXTENDS";
				}
				else if (lexeme.toLowerCase() == "return"){
					return "RETURN";
				}
				else if (lexeme.toLowerCase() == "int"){
					return "INT";
				}
				else if (lexeme.toLowerCase() == "double"){
					return "DOUBLE";
				}
				else if (lexeme.toLowerCase() == "boolean"){
					return "BOOLEAN";
				}
				else if (lexeme.toLowerCase() == "if"){
					return "IF";
				}
				else if (lexeme.toLowerCase() == "while"){
					return "WHILE";
				}
				else if (lexeme == "System.out.print"){
					return "SYSTEM.OUT.PRINT";
				}
				else if (lexeme.toLowerCase() == "length"){
					return "LENGTH";
				}
				else if (lexeme.toLowerCase() == "true"){
					return "TRUE";
				}
				else if (lexeme.toLowerCase() == "false"){
					return "FALSE";
				}
				else if (lexeme.toLowerCase() == "this"){
					return "THIS";
				}
				else if (lexeme.toLowerCase() == "new"){
					return "NEW";
				}
				else if (lexeme == "Xinu.print"){
					return "PRINT";
				}
				else if (lexeme == "Xinu.println"){
					return "PRINTLN";
				}
				else if (lexeme == "Xinu.printint"){
					return "PRINTINT";
				}
				else if (lexeme == "Xinu.readint"){
					return "READINT";
				}
			case COMMENT_BUILDING:
				lexeme = lexeme + (char) c;
				if (Debug) System.out.println("Currentl reading" + c);
				c = reader.read();
				break;
			case SINGLE_COMMENT:
				lexeme =  lexeme + (char) c;
				c = reader.read();
				break;
			case MULTI_COMMENT:
				lexeme =  lexeme + (char) c;
                                c = reader.read();
                                break;	
			case COMMENT_ACCEPT:
				lexeme =  lexeme + (char) c;
                                c = reader.read();
                                break;
			case ERR:
				//Temp message will need to put out required Error
				int numErr = 0;
				for (int i = 0; i < lexeme.length(); i++){
					if(!Character.isDigit(lexeme.charAt(i))){
						if(lexeme.substring(0,2) == "0x"){
							numErr = 1;
						}
						else if(lexeme.charAt(0) == '0'){
							numErr = 2;
						}
						else if(lexeme.charAt(0) != '0'){
							numErr = 3;
						}
					}
				}
				if((lexeme.substring(0,2) == "/*") && (lexeme.substring(lexeme.length()-2)) != "*/"){
					return "Comment not terminated at end of input";
				}
				else if(numErr == 1){
					return "Invalid character in hex number.";
				}
				else if(numErr == 2){
					return "Invalid character in octal number.";
				}
				else if(numErr == 3){
					return "Invalid character in number.";
				}
				else if((lexeme.charAt(0) == '"') && (lexeme.charAt(lexeme.length()-1) != '"')){
					return "String not terminated at end of line.";
				}
				else{
					return "Illegal token.";
				}
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

