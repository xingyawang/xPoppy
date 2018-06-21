import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/***********************************************/
/*  assgnment.5  Shu Z. A00042813 for CS453    */
/*  using the tokenizer and stream module      */
/*  print_tokens.c Code                        */
/***********************************************/

/***********************************************/
/* NAME:	print_tokens                   */
/* INPUT:	a filename                     */
/* OUTPUT:      print out the token stream     */
/* DESCRIPTION: using the tokenizer interface  */
/*              to print out the token stream  */
/***********************************************/

public class PrintTokens2 {
	public static int error = 0;
	public static int keyword = 1;
	public static int spec_symbol = 2;
	public static int identifier = 3;
	public static int num_constant = 41;
	public static int str_constant = 42;
	public static int char_constant = 43;
	public static int comment = 5;
	public static int end = 6;

	public static final int EOF = -1;

	public static void main(String[] argv)
	{  
		String fname = null;
		char[] tok;
		BufferedReader tp;
		if(argv.length==0)                  /* if not given filename,take as '""' */
		{
			fname= "";
		}
		else if(argv.length==1) {
			fname = argv[0];
		}
		else
		{ 
			System.out.print("Error!,please give the token stream\n");
			System.exit(0);
		}
		tp=open_token_stream(fname);  /* open token stream */
		tok=get_token(tp);
		while (is_eof_token(tok) ==false) /* take one token each time until eof */
		{
			print_token(tok);
			tok=get_token(tp);
		}
		print_token(tok); /* print eof signal */
		System.exit(0);
	}

	/* stream.c code */

	/***********************************************/
	/* NMAE:	open_character_stream          */
	/* INPUT:       a filename                     */
	/* OUTPUT:      a pointer to chacracter_stream */
	/* DESCRIPTION: when not given a filename,     */
	/*              open stdin,otherwise open      */
	/*              the existed file               */
	/***********************************************/
	static BufferedReader open_character_stream(String fname)
	{ 
		BufferedReader fp = null;
		if(fname == null)
			fp= new BufferedReader(
					new InputStreamReader(System.in));
		else
		{
			try {
				fp = new BufferedReader (
						new FileReader(fname));
			} catch (IOException e) {
				System.out.print("The file " + fname +" doesn't exists\n");
				System.exit(0);
			}
		}
		return(fp);
	}

	/**********************************************/
	/* NAME:	get_char                      */
	/* INPUT:       a pointer to char_stream      */
	/* OUTPUT:      a character                   */
	/**********************************************/
	static char get_char(BufferedReader fp)
	{ 
		char ch = (char)EOF;
//		ch=getc(fp);
		try {
			fp.mark(10);
			ch = (char) fp.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return(ch);
	}

	/***************************************************/
	/* NAME:      unget_char                           */
	/* INPUT:     a pointer to char_stream,a character */
	/* OUTPUT:    a character                          */
	/* DESCRIPTION:when unable to put back,return EOF  */
	/***************************************************/
	static char unget_char(char ch, BufferedReader fp)
	{ 
		char c = 0;
//		c=ungetc(ch,fp);
		try {
			fp.reset();
		} catch (IOException e) {
			c = (char)EOF;
		}
		
		if(c == (char)EOF)
		{
			return(c);
		}
		else
			return(ch);
	}

	/* tokenizer.c code */


	static char[] buffer = new char[81];  /* fixed array length MONI */ /* to store the token temporar */



	/********************************************************/
	/* NAME:	open_token_stream                       */
	/* INPUT:       a filename                              */
	/* OUTPUT:      a pointer to a token_stream             */
	/* DESCRIPTION: when filename is EMPTY,choice standard  */
	/*              input device as input source            */
	/********************************************************/
	static BufferedReader open_token_stream(String fname)
	{
		BufferedReader fp;
		if(fname == "")
			fp= open_character_stream(null);
		else
			fp= open_character_stream(fname);
		return(fp);
	}

	/********************************************************/
	/* NAME :	get_token                               */
	/* INPUT: 	a pointer to the tokens_stream          */
	/* OUTPUT:      a token                                 */
	/* DESCRIPTION: according the syntax of tokens,dealing  */
	/*              with different case  and get one token  */
	/********************************************************/
	static char[] get_token(BufferedReader tp)
	{ 
		int i=0,j;
		int id=0;
		char ch;
		char[] ch1 = new char[2];
		for (j=0;j<=80;j++)          /* initial the buffer   */
		{
			buffer[j]='\0';
		} 
		ch1[0]='\0';
		ch1[1]='\0';
		ch=get_char(tp);
		while(ch==' '||ch=='\n')      /* strip all blanks until meet characters */
		{
			ch=get_char(tp);
		} 
		buffer[i]=ch;
		if(is_eof_token(buffer)==true)
			return(buffer);
		if(is_spec_symbol(buffer)==true)
			return(buffer); 
		if(ch =='"')id=1;    /* prepare for string */
		if(ch ==59)id=2;    /* prepare for comment */
		ch=get_char(tp);

		while (is_token_end(id,ch) == false)/* until meet the end character */
		{
			i++;
			try {
				buffer[i]=ch;
			} catch(ArrayIndexOutOfBoundsException e) {
				// JAVA : reproducing overflow bug in C
			}
			ch=get_char(tp);
		}
		ch1[0]=ch;                        /* hold the end charcater          */
		if(is_eof_token(ch1)==true)       /* if end character is eof token    */
		{ 
			ch=unget_char(ch,tp);        /* then put back eof on token_stream */
			if(ch==(char)EOF)
				unget_error(tp);
			return(buffer);
		}
		if(is_spec_symbol(ch1)==true)     /* if end character is special_symbol */
		{
			ch=unget_char(ch,tp);        /* then put back this character       */
			if(ch==(char)EOF)
				unget_error(tp);
			return(buffer);
		}
		if(id==1)                  /* if end character is " and is string */
		{ 
			i++;                     /* case,hold the second " in buffer    */
			buffer[i]=ch;
			return(buffer); 
		}
		if(id==0 && ch==59)
			/* when not in string or comment,meet ";" */
		{ 
			ch=unget_char(ch,tp);       /* then put back this character         */
			if(ch==(char)EOF)
				unget_error(tp);
			return(buffer); 
		}
		return(buffer);                   /* return nomal case token             */
	}

	/*******************************************************/
	/* NAME:	is_token_end                           */
	/* INPUT:       a character,a token status             */
	/* OUTPUT:	a BOOLEAN value                        */
	/*******************************************************/
	static boolean is_token_end(int str_com_id,char ch)
	{ 
		char[] ch1 = new char[2];  /* fixed array declaration MONI */
		ch1[0]=ch;
		ch1[1]='\0';
		if(is_eof_token(ch1)==true)
			return(true); /* is eof token? */
		if(str_com_id==1)          /* is string token */
		{ 
			if(ch=='"' | ch=='\n')   /* for string until meet another " */
				return(true);
			else
				return(false);
		}

		if(str_com_id==2)    /* is comment token */
		{ 
			if(ch=='\n')     /* for comment until meet end of line */
				return(true);
			else
				return(false);
		}

		if(is_spec_symbol(ch1)==true) 
			return(true); /* is special_symbol? */
		if(ch ==' ' || ch=='\n' || ch==59) 
			return(true); 
		/* others until meet blank or tab or 59 */
		return(false);               /* other case,return FALSE */
	}

	/****************************************************/
	/* NAME :	token_type                          */
	/* INPUT:       a pointer to the token              */
	/* OUTPUT:      an integer value                    */
	/* DESCRIPTION: the integer value is corresponding  */
	/*              to the different token type         */
	/****************************************************/
	static int token_type(char[] tok)
	{ 
		if(is_keyword(tok))return(keyword);
		if(is_spec_symbol(tok))return(spec_symbol);
		if(is_identifier(tok))return(identifier);
		if(is_num_constant(tok))return(num_constant);
		if(is_str_constant(tok))return(str_constant);
		if(is_char_constant(tok))return(char_constant);
		if(is_comment(tok))return(comment);
		if(is_eof_token(tok))return(end);
		return(error);                    /* else look as error token */
	}

	/****************************************************/
	/* NAME:	print_token                         */
	/* INPUT:	a pointer to the token              */
	/* OUTPUT:      a BOOLEAN value,print out the token */
	/*              according the forms required        */
	/****************************************************/
	static boolean print_token(char[] tok)
	{ 
		int type;
		type=token_type(tok);
		String token = szString(tok, 0);
		if(type==error)
		{ 
			System.out.print("error,\"" + token +"\".\n");
		} 
		if(type==keyword)
		{
			System.out.print("keyword,\"" + token +"\".\n");
		}
		if(type==spec_symbol)
			print_spec_symbol(tok);
		if(type==identifier)
		{
			System.out.print("identifier,\"" + token +"\".\n");
		}
		if(type==num_constant)
		{
			System.out.print("numeric,"+token+".\n");
		}
		if(type==str_constant)
		{
			System.out.print("string,"+token+".\n");
		}
		if(type==char_constant)
		{
//			tok=tok+1;
			token = szString(tok, 1);
			System.out.print("character,\"" + token +"\".\n");
		}
		if(type==end) 
			System.out.print("eof.\n");
		// JAVA added return statement
		return true;
	}

	private static String szString(char[] tok, int start_ind) {
		StringBuffer sb = new StringBuffer();
		int i = start_ind;
		try {
    	while(tok[i] != '\0')
			sb.append(tok[i++]);
		}catch(ArrayIndexOutOfBoundsException e) {
			// JAVA : reproducing the overflow bug in C
		}
    	return sb.toString();
	}

	/* the code for tokens judgment function */

	/*************************************/
	/* NAME:	is_eof_token         */
	/* INPUT: 	a pointer to a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_eof_token(char[] tok)
	{ 
		if( tok[0]==(char)EOF)
			return(true);
		else
			return(false);
	}

	/*************************************/
	/* NAME:	is_comment           */
	/* INPUT: 	a pointer to a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_comment(char[] ident)
	{
		if( (ident[0]) ==59 )   /* the char is 59   */
			return(true);
		else
			return(false);
	}

	/*************************************/
	/* NAME:	is_keyword           */
	/* INPUT: 	a pointer to a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_keyword(char[] str)
	{ 
		if (!strcmp(str,"and") || !strcmp(str,"or") || !strcmp(str,"if") ||
				!strcmp(str,"xor")||!strcmp(str,"lambda")||!strcmp(str,"=>"))
			return(true);
		else 
			return(false);
	}

	private static boolean strcmp(char[] str, String string) {
		String s = szString(str, 0);
		return (!s.equals(string));
	}

	/*************************************/
	/* NAME:	is_char_constant     */
	/* INPUT: 	a pointer to a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_char_constant(char[] str)
	{
		if ((str[0])=='#' && Character.isLetter(str[1]))
			return(true);
		else  
			return(false);
	}

	/*************************************/
	/* NAME:	is_num_constant      */
	/* INPUT: 	a pointer to a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_num_constant(	char[]  str)
	{
		int i=1;

		if ( Character.isDigit(str[0])) 
		{
			while ( str[i] != '\0' )   /* until meet token end sign */
			{
				if(Character.isDigit(str[i]))
					i++;
				else
					return(false);
			}                         /* end WHILE */
			return(true);
		}
		else
			return(false);               /* other return FALSE */
	}

	/*************************************/
	/* NAME:	is_str_constant      */
	/* INPUT: 	a pointer to a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_str_constant(	char[] str)
	{
		int i=1;

		if ( str[0] == '"')
		{ 
			while (str[i] !='\0')  /* until meet the token end sign */
			{ 
				if(str[i]=='"')
					return(true);        /* meet the second '"'           */
				else
					i++;
			}               /* end WHILE */

			return(false);
		}
		else
			return(false);       /* other return FALSE */
	}
	/*************************************/
	/* NAME:	is_identifier         */
	/* INPUT: 	a pointer to a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_identifier(	char[]  str)

	{
		int i=1;

		if ( Character.isLetter( str[0]) ) 
		{
			while(  str[i] !='\0' )   /* unti meet the end token sign */
			{ 
				if(Character.isLetter(str[i]) || Character.isDigit(str[i]))   
					i++;
				else
					return(false);
			}      /* end WHILE */
			return(true);
		}
		else
			return(false);
	}

	/******************************************/
	/* NAME:	unget_error               */
	/* INPUT:       a pointer to token stream */
	/* OUTPUT: 	print error message       */
	/******************************************/
	static void unget_error(BufferedReader fp)

	{
		System.out.print("It can not get charcter\n");
	}

	/*************************************************/
	/* NAME:        print_spec_symbol                */
	/* INPUT:       a pointer to a spec_symbol token */
	/* OUTPUT :     print out the spec_symbol token  */
	/*              according to the form required   */
	/*************************************************/
	static void print_spec_symbol(	char[] str)

	{
		if      (!strcmp(str,"("))
		{
			System.out.println("lparen.");
			return;
		} 
		if (!strcmp(str,")"))
		{
			System.out.println("rparen.");
			return;
		}
		if (!strcmp(str,"["))
		{
			System.out.println("lsquare.");
			return;
		}
		if (!strcmp(str,"]"))
		{
			System.out.println("rsquare.");
			return;
		}
		if (!strcmp(str,"'"))
		{
			System.out.println("quote.");
			return;
		}
		if (!strcmp(str,"`"))
		{
			System.out.println("bquote.");
			return;
		}

		System.out.println("comma.");
	}


	/*************************************/
	/* NAME:        is_spec_symbol       */
	/* INPUT:       a pointer to a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_spec_symbol(char[] str)
	{
		if (!strcmp(str,"("))
		{  
			return(true);
		}
		if (!strcmp(str,")"))
		{
			return(true);
		}
		if (!strcmp(str,"["))
		{
			return(true);
		}
		if (!strcmp(str,"]"))
		{
			return(true);
		}
		if (!strcmp(str,"'"))
		{
			return(true);
		}
		if (!strcmp(str,"`"))
		{
			return(true);
		}
		if (!strcmp(str,","))
		{
			return(true);
		}
		return(false);     /* others return FALSE */
	}


}