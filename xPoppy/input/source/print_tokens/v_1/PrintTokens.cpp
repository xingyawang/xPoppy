import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;



public class PrintTokens {

    public static final int START = 5;

    public static final int EOF = -1;
    //typedef char *string;
    static class character_stream {
        BufferedReader fp;
        int stream_ind; /*buffer index */
        char[] stream = new char[80];
    }

    static class token_stream {
        character_stream ch_stream;
    }

    static class token {
           int token_id;
           char[] token_string = new char[80];
    }

    public static void main(String[] argv)
    {
        token token_ptr;
        token_stream stream_ptr;

        if(argv.length>1)
        {
            System.out.print( "The format is print_tokens filename(optional)\n");
            System.exit(1);
        }
        if (argv.length == 1)
            stream_ptr=open_token_stream(argv[0]);
        else
            stream_ptr=open_token_stream(null);

        while(!is_eof_token((token_ptr=get_token(stream_ptr))))
            print_token(token_ptr);
        print_token(token_ptr);
        System.exit(0);
    }



    /* *********************************************************************
       Function name : open_character_stream
Input         : filename 
Output        : charactre stream.
Exceptions    : If file name doesn't exists it will
exit from the program.
Description   : The function first allocates the memory for 
the structure and initilizes it. The constant
START gives the first character available in
the stream. It ckecks whether the filename is
empty string. If it is it assigns file pointer
to stdin else it opens the respective file as input.                   * ******************************************************************* */

    static character_stream open_character_stream(String FILENAME) {
        character_stream stream_ptr;

        stream_ptr= new character_stream();
        stream_ptr.stream_ind=START;
        stream_ptr.stream[START]='\0';
        if(FILENAME == null)
            stream_ptr.fp= new BufferedReader(new InputStreamReader(System.in));
        else
        {
            try {
                stream_ptr.fp = new BufferedReader(new FileReader(FILENAME));
            } catch(IOException e) {
                System.out.print("The file " + FILENAME + " doesn't exists\n");
                System.exit(0);
            }
        }
        return(stream_ptr);
    }

    /* *********************************************************************
       Function name : get_char
Input         : charcter_stream.
Output        : character.
Exceptions    : None.
Description   : This function takes character_stream type variable 
as input and returns one character. If the stream is
empty then it reads the next line from the file and
returns the character.       
     * ****************************************************************** */

    static char get_char(character_stream stream_ptr)
    {
        if(stream_ptr.stream[stream_ptr.stream_ind] == '\0')
        {
            if(C_stdlib.fgets(stream_ptr.stream, START ,80-START,stream_ptr.fp) == null)/* Fix bug: add -START - hf*/
                stream_ptr.stream[START]=(char) EOF;
            stream_ptr.stream_ind=START;
        }
        return(stream_ptr.stream[(stream_ptr.stream_ind)++]);
    }

    /* *******************************************************************
       Function name : is_end_of_character_stream.
Input         : character_stream.
Output        : Boolean value.
Description   : This function checks whether it is end of character
stream or not. It returns BOOLEANvariable which is 
true or false. The function checks whether the last 
read character is end file character or not and
returns the value according to it.
     * ****************************************************************** */

    static boolean is_end_of_character_stream(character_stream stream_ptr)
    {
        if(stream_ptr.stream[stream_ptr.stream_ind-1] == (char)EOF)
            return(true);
        else
            return(false);
    }

    /* *********************************************************************
       Function name : unget_char
Input         : character,character_stream.
Output        : void.
Description   : This function adds the character ch to the stream. 
This is accomplished by decrementing the stream_ind
and storing it in the stream. If it is not possible
to unget the character then it returns
     * ******************************************************************* */

    static void unget_char(char ch,character_stream stream_ptr)
    {
        if(stream_ptr.stream_ind == 0)
            return;
        else
            stream_ptr.stream[--(stream_ptr.stream_ind)]=ch;
        return;
    }


    /* *******************************************************************
       Function name : open_token_stream
Input         : filename
Output        : token_stream
Exceptions    : Exits if the file specified by filename not found.
Description   : This function takes filename as input and opens the
token_stream which is nothing but the character stream.
This function allocates the memory for token_stream 
and calls open_character_stream to open the file as
input. This function returns the token_stream.
     * ****************************************************************** */

    static token_stream open_token_stream(String FILENAME)
    {
        token_stream token_ptr;

        token_ptr= new token_stream();
        token_ptr.ch_stream=open_character_stream(FILENAME);/* Get character
                                                               stream  */
        return(token_ptr);
    }

    /* ********************************************************************
       Function name : get_token
Input         : token_stream
Output        : token
Exceptions    : none.
Description   : This function returns the next token from the
token_stream.The type of token is integer and specifies 
only the type of the token. DFA is used for finding the
next token. cu_state is initialized to zero and charcter
are read until the the is the final state and it
returns the token type.
     * ******************************************************************* */

    static token get_token(token_stream tstream_ptr)
    {
        char[] token_str = new char[80]; /* This buffer stores the current token */
        int token_ind;      /* Index to the token_str  */
        token token_ptr;
        char ch;
        int cu_state,next_st,token_found;

        token_ptr=new token();
        ch=get_char(tstream_ptr.ch_stream);
        cu_state=token_ind=token_found=0;
        while(token_found == 0)
        {
            if(token_ind < 80) /* ADDED ERROR CHECK - hf */
            {
                token_str[token_ind++]=ch;
                next_st=next_state(cu_state,ch);
            }
            else
            {
                next_st = -1; /* - hf */
            }
            if (next_st == -1) { /* ERROR or EOF case */
                return(error_or_eof_case(tstream_ptr, 
                            token_ptr,cu_state,token_str,token_ind,ch));
            } else if (next_st == -2) {/* This is numeric case. */
                return(numeric_case(tstream_ptr,token_ptr,ch,
                            token_str,token_ind));
            } else if (next_st == -3) {/* This is the IDENTIFIER case */
                token_ptr.token_id=Constants.IDENTIFIER;
                unget_char(ch,tstream_ptr.ch_stream);
                token_ind--;
                get_actual_token(token_str,token_ind);
                strcpy(token_ptr.token_string,token_str);
                return(token_ptr);
            } 

            switch(next_st) 
            { 
                default : break;
                case 6  : /* These are all KEYWORD cases. */
                case 9  :
                case 11 :
#ifdef F_PT_HD_2
                case 12 :   case 13 :
#else
                case 13 :
#endif
#ifdef F_PT_HD_1
                case 16 :
                case 32 : ch=get_char(tstream_ptr.ch_stream);
                          if(check_delimiter(ch)==true)
                          {
                              token_ptr.token_id=keyword(next_st);
                              unget_char(ch,tstream_ptr.ch_stream);
                              token_ptr.token_string[0]='\0';
                              return(token_ptr);
                          }
                          unget_char(ch,tstream_ptr.ch_stream);
                          break;
                case 19 : /* These are all special SPECIAL character */
                case 20 : /* cases */
                case 21 :
                case 22 :
                case 23 :
                case 24 :
                case 25 : token_ptr.token_id=special(next_st);
                          token_ptr.token_string[0]='\0';
                          return(token_ptr);
#elif F_PT_HD_3
                case 16 : ch=get_char(tstream_ptr.ch_stream);
                          if(check_delimiter(ch)==true)
                          {
                              token_ptr.token_id=keyword(next_st);
                              unget_char(ch,tstream_ptr.ch_stream);
                              token_ptr.token_string[0]='\0';
                              return(token_ptr);
                          }
                         /* unget_char(ch,tstream_ptr.ch_stream);*/
                          break;
                case 19 : /* These are all special SPECIAL character */
                case 20 : /* cases */
                case 21 :
                case 22 :
                case 23 :
                case 24 :
                case 25 :
                case 32 : token_ptr.token_id=special(next_st);
                          token_ptr.token_string[0]='\0';
                          return(token_ptr);
#else
                case 16 : ch=get_char(tstream_ptr.ch_stream);
                          if(check_delimiter(ch)==true)
                          {
                              token_ptr.token_id=keyword(next_st);
                              unget_char(ch,tstream_ptr.ch_stream);
                              token_ptr.token_string[0]='\0';
                              return(token_ptr);
                          }
                          unget_char(ch,tstream_ptr.ch_stream);
                          break;
                case 19 : /* These are all special SPECIAL character */
                case 20 : /* cases */
                case 21 :
                case 22 :
                case 23 :
                case 24 :
                case 25 :
                case 32 : token_ptr.token_id=special(next_st);
                          token_ptr.token_string[0]='\0';
                          return(token_ptr);
#endif
                case 27 : /* These are constant cases */
                case 29 : token_ptr.token_id=constant(next_st,token_str,token_ind);
                          get_actual_token(token_str,token_ind);
                          strcpy(token_ptr.token_string,token_str);
                          return(token_ptr);
                case 30 :  /* This is COMMENT case */
                          skip(tstream_ptr.ch_stream);
#ifdef F_PT_HD_5
                          /*token_ind=*/next_st=0;
#else
                          token_ind=next_st=0;
#endif
                          break;
            }
            cu_state=next_st;
            ch=get_char(tstream_ptr.ch_stream);
        }
        //JAVA : added return statement 
        return null;
    }

    /* ******************************************************************
       Function name : numeric_case
Input         : tstream_ptr,token_ptr,ch,token_str,token_ind
Output        : token_ptr;
Exceptions    : none 
Description   : It checks for the delimiter, if it is then it
forms numeric token else forms error token.
     * ****************************************************************** */

    static token numeric_case(token_stream tstream_ptr, token token_ptr,char ch,char[] token_str,int token_ind)
    {
        if(check_delimiter(ch)!=true)
        {   /* Error case */
            token_ptr.token_id=Constants.ERROR;
            while(check_delimiter(ch)==false)
            {
#ifdef F_PT_HD_7
                if(token_ind >= 10) break; /* Added protection - hf */
#else
                if(token_ind >= 80) break; /* Added protection - hf */
#endif    
                token_str[token_ind++]=ch=get_char(tstream_ptr.ch_stream);
            }
            unget_char(ch,tstream_ptr.ch_stream);
            token_ind--;
            get_actual_token(token_str,token_ind);
            strcpy(token_ptr.token_string,token_str);
            return(token_ptr);
        }
        token_ptr.token_id=Constants.NUMERIC; /* Numeric case */
        unget_char(ch,tstream_ptr.ch_stream);
        token_ind--;
        get_actual_token(token_str,token_ind);
        strcpy(token_ptr.token_string,token_str);
        return(token_ptr);
    }

    private static void strcpy(char[] token_string, char[] token_str) {
        int i = 0;
        while (token_str[i] != '\0') {
            token_string[i] = token_str[i];
            i++;
        }
        token_string[i] = '\0';
    }


    /* *****************************************************************
       Function name : error_or_eof_case 
Input         : tstream_ptr,token_ptr,cu_state,token_str,token_ind,ch
Output        : token_ptr 
Exceptions    : none 
Description   : This function checks whether it is EOF or not.
If it is it returns EOF token else returns ERROR 
token.
     * *****************************************************************/
    static token error_or_eof_case(token_stream tstream_ptr,token token_ptr,int cu_state,char token_str[],int token_ind,char ch)
    {
        if(is_end_of_character_stream(tstream_ptr.ch_stream)) 
        {
            token_ptr.token_id = Constants.EOTSTREAM;
            token_ptr.token_string[0]='\0';
            return(token_ptr);
        }
        if(cu_state !=0)
        {
            unget_char(ch,tstream_ptr.ch_stream);
            token_ind--;
        }
        token_ptr.token_id=Constants.ERROR;
        get_actual_token(token_str,token_ind);
        strcpy(token_ptr.token_string,token_str);
        return(token_ptr);                
    }

    /* *********************************************************************
       Function name : check_delimiter
Input         : character
Output        : boolean
Exceptions    : none.
Description   : This function checks for the delimiter. If ch is not
alphabet and non numeric then it returns TRUE else 
it returns FALSE. 
     * ******************************************************************* */

    static boolean check_delimiter(char ch)
    {
        if(!Character.isLetter(ch) && !Character.isDigit(ch)) /* Check for digit and alpha */
            return(true);
        return(false);
    }

    /* ********************************************************************
       Function name : keyword
Input         : state of the DFA
Output        : Keyword.
Exceptions    : If the state doesn't represent a keyword it exits.
Description   : According to the final state specified by state the
respective token_id is returned.
     * ***************************************************************** */

    static int keyword(int state)
    {
        switch(state)
        {   /* Return the respective macro for the Keyword. */
            case 6 : return(Constants.LAMBDA);
            case 9 : return(Constants.AND);
            case 11: return(Constants.OR);
            case 13: return(Constants.IF);
            case 16: return(Constants.XOR);
            default: System.out.print( "error\n");break;
        }
        System.exit(0);
        return state;
    }

    /* ********************************************************************
       Function name : special
Input         : The state of the DFA.
Output        : special symbol.
Exceptions    : if the state doesn't belong to a special character
it exits.
Description   : This function returns the token_id according to the
final state given by state.
     * ****************************************************************** */

    static int special(int state)
    {
        switch(state)
        {   /* return the respective macro for the special character. */
            case 19: return(Constants.LPAREN);
            case 20: return(Constants.RPAREN);
            case 21: return(Constants.LSQUARE);
            case 22: return(Constants.RSQUARE);
            case 23: return(Constants.QUOTE);
            case 24: return(Constants.BQUOTE);
            case 25: return(Constants.COMMA);
            case 32: return(Constants.EQUALGREATER);
            default: System.out.print("error\n");break;
        }
        System.exit(0);
        return state;
    }

    /* **********************************************************************
       Function name : skip
Input         : character_stream
Output        : void.
Exceptions    : none.
Description   : This function skips the comment part of the program.
It takes charcter_stream as input and reads character
until it finds new line character or
end_of_character_stream.                   
     * ******************************************************************* */

    static void skip(character_stream stream_ptr)
    {
        char c;

        while((c=get_char(stream_ptr))!='\n' && 
                !is_end_of_character_stream(stream_ptr))
            ; /* Skip the characters until EOF or EOL found. */
        if(c==(char)EOF) 
            unget_char(c, stream_ptr); /* Put back to leave gracefully - hf */
        return;
    }

    /* *********************************************************************
       Function name : constant
Input         : state of DFA, Token string, Token id.
Output        : constant token.
Exceptions    : none.
Description   : This function returns the token_id for the constatnts
speccified by  the final state. 
     * ****************************************************************** */

    static int constant(int state,char[] token_str,int token_ind)
    {
        switch(state)
        {   /* Return the respective CONSTANT macro. */
            case 27 : return(Constants.STRING_CONSTANT);
            case 29 : token_str[token_ind-2]=' '; return(Constants.CHARACTER_CONSTANT);
            default : break;
        }
        // JAVA : added return statement to compile
        return -1;
    }


    /* *******************************************************************
       Function name : next_state
Input         : current state, character
Output        : next state of the DFA
Exceptions    : none.
Description   : This function returns the next state in the transition
diagram. The next state is determined by the current
state state and the inpu character ch.
     * ****************************************************************** */

    static int next_state(int state,char ch)
    {
        if(state < 0)
            return(state);
        // JAVA : char cannot hold -1, so this is a boundary case hack
        int ch_value;
        if (ch == (char)EOF)
            ch_value = -1;
        else 
            ch_value = ch;
        if(Constants.base[state]+ch_value >= 0)
        {
            try {
                if(Constants.check[Constants.base[state]+ch_value] == state) /* Check for the right state */
                    return(Constants.next[Constants.base[state]+ch_value]);
                else
                    return(next_state(Constants.default1[state],ch));
            } catch(ArrayIndexOutOfBoundsException e) {
                // JAVA : this is an actual bug but in order to be compatible with the C
                // version, we need this workaround
                return(next_state(Constants.default1[state],ch));
            }
        }
        else
            return(next_state(Constants.default1[state],ch));
    }

    /* *********************************************************************
       Function name : is_eof_token
Input         : token
Output        : Boolean
Exceptions    : none.
Description   : This function checks whether the token t is eof_token 
or not. If the integer value stored in the t is
EOTSTREAM then it is eof_token.
     * ***************************************************************** */

    static boolean is_eof_token(token t)
    {
        if(t.token_id==Constants.EOTSTREAM)
            return(true);
        return(false);
    }

    /* ********************************************************************
       Function name : print_token
Input         : token
Output        : Boolean
Exceptions    : none.
Description   : This function  prints the token. The token_id gives 
the type of token not the token itself. So, in the
case of identifier,numeric,  string,character it is
required to print the actual token  from token_str. 
So, precaution must be taken when printing the token.
This function is able to print the current token only
and it is the limitation of the program.
     * ******************************************************************** */

    static boolean print_token(token token_ptr)
    {
        switch(token_ptr.token_id)
        {    /* Print the respective tokens. */
            case Constants.ERROR : System.out.print( "error,\t\"");
                printf(token_ptr.token_string);
                System.out.print("\".\n");
                return(true);
            case Constants.EOTSTREAM : System.out.print( "eof.\n");return(true);
            case 6 : System.out.print( "keyword,\t\"lambda\".\n");return(true);
            case 9 : System.out.print( "keyword,\t\"and\".\n");return(true);
            case 11: System.out.print( "keyword,\t\"or\".\n");return(true);
            case 13: System.out.print( "keyword,\t\"if\".\n");return(true);
            case 16: System.out.print( "keyword,\t\"xor\".\n");return(true);
            case 17: System.out.print( "identifier,\t\"");
            printf(token_ptr.token_string);
            System.out.print( "\".\n");return(true);
            case 18: System.out.print( "numeric,\t");
            printf(token_ptr.token_string);
            System.out.print( ".\n");return(true);
            case 19: System.out.print( "lparen.\n");return(true);
            case 20: System.out.print( "rparen.\n");return(true);
            case 21: System.out.print( "lsquare.\n");return(true);
            case 22: System.out.print( "rsquare.\n");return(true);
            case 23: System.out.print( "quote.\n");return(true);
            case 24: System.out.print( "bquote.\n");return(true);
            case 25: System.out.print( "comma.\n");return(true);
            case 27: System.out.print( "string,\t");
            printf(token_ptr.token_string);
            System.out.print( ".\n");return(true);
            case 29: System.out.print( "character,\t\"");
            printf( token_ptr.token_string);
            System.out.print( "\".\n");return(true);
            case 32: System.out.print( "keyword,\t\"=>\".\n");return(true);
            default: break;
        }
        return(false);
    }

    private static void printf(char[] token_string) {
        int i = 0;
        while(token_string[i] != '\0')
            System.out.print(token_string[i++]);
    }



    /* **********************************************************************
       Function name : get_actual_token
Input         : token string and token id.
Output        : void.
Exceptions    : none.
Description   : This function prints the actual token in the case of
identifier,numeric,string and character. It removes
the leading and trailing  spaces and prints the token.
     * ****************************************************************** */

    static void get_actual_token(char[] token_str,int token_ind)
    {
        int ind,start;

        for(ind=token_ind;ind>0 && Character.isWhitespace(token_str[ind-1]);--ind); 
        /* Delete the trailing white spaces & protect - hf */
        token_str[ind]='\0';token_ind=ind;
        for(ind=0;ind<token_ind;++ind)
            if(!Character.isWhitespace(token_str[ind]))
                break;
        for(start=0;ind<=token_ind;++start,++ind) /* Delete the leading
                                                     white spaces. */
            token_str[start]=token_str[ind];
        return;
    }
}
