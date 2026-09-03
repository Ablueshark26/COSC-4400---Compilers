/**
 * COSC 4400 - Project #1
 * Explain briefly the functionality of the program.
 * @authors Luke Sam 
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO luke.sharba@marquette.edu 
 * TA-BOT:MAILTO samuel.biskupic@marquette.edu
 */
import java.io.BufferedReader;                                              import java.io.InputStreamReader;      

public class Scanner{
	
	
	public enum CharType
	{ LETTER, DIGIT, OPERATORS, PUNC, STR, OTHER};
      	
	public CharType characterClass[] = new CharType[256];

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
                characterClass[38] = CharType.OPERATORS;
		characterClass[124] = CharType.OPERATORS;
		characterClass[94] = CharType.OPERATORS;
		characterClass[126] = CharType.OPERATORS;
		characterClass[43] = CharType.OPERATORS;
		characterClass[45] = CharType.OPERATORS;
		characterClass[42] = CharType.OPERATORS;
		characterClass[47] = CharType.OPERATORS;
		characterClass[60] = CharType.OPERATORS;
		characterClass[62] = CharType.OPERATORS;
		characterClass[61] = CharType.OPERATORS;
		characterClass[33] = CharType.OPERATORS;
		characterClass[] = CharType.
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
