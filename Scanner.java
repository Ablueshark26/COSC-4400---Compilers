public class Scanner{
	
	
	public enum CharType
	{ LETTER, DIGIT, BW, LOGICAL, MATH, COMP, UNI, PUNC, STR, OTHER};
      	
	public CharType characterClass[] = new CharType[256];

	public Scanner()
	{
		for(int i = 0; i < characterClass.length)
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
