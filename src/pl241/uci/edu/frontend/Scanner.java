package pl241.uci.edu.frontend;


import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
Data:2015/03/02
This class is for the scanner of the code.
 */
public class Scanner {
    private FileReader fReader;
    private Character curChar;

    //if we get a number, store it in val
    public int val;

    //this is the id of predefined function
    public int id;

    public static ArrayList<String> preDefinedFunction; // keep current identIdx and List[existidents] to save space

    // Constructor: open file and scan the first token into 'inputSym'
    public Scanner(String fileName) throws FileNotFoundException, IOException {
        fReader = new FileReader(fileName);
        curChar = null;
        id = -1;
        preDefinedFunction = new ArrayList<String>();
        preDefinedFunction.add("InputNum");
        preDefinedFunction.add("OutputNum");
        preDefinedFunction.add("OutputNewLine");;
    }

    public void startScanner() throws IOException {
        fReader.openFile();
        curChar = fReader.getCurrentChar();
    }

    public void closeScanner() throws IOException {
        fReader.closeFile();
    }

    // Advance to the next character
    private void nextChar() throws IOException {
        curChar = fReader.getNextChar();
    }

    // Advance to the first character of next line
    private void nextLine() throws IOException {
        fReader.getNextLine();
        curChar = fReader.getCurrentChar();
    }

    // Advance to the next token
    public Token getNextToken() throws IOException {
        Token curToken = null;
        // Skip space and comment
        if ((curToken = skipSpaceAndComment()) != null)
            return curToken;

        // check if eof
        if (curChar == '~')
            return Token.EOF;

        // Check if it is number token
        if ((curToken = getNumberToken()) != null)
            return curToken;

        // Check if it is a letter token(including ident and keyword token)
        if ((curToken = getLetterToken()) != null)
            return curToken;

        // otherwise, other tokens
        switch (curChar) {
            // if operand(+,-,*,/)
            case '+':
                nextChar();
                return Token.PLUS;
            case '-':
                nextChar();
                return Token.MINUS;
            case '*':
                nextChar();
                return Token.TIMES;
            // if comparison, here pay attention to designator('<-')
            case '=':
                nextChar();
                if (curChar == '=') {
                    nextChar();
                    return Token.EQL;
                } else {
                    //error
                    this.Error("Scanner Error!"+"\"=\" should be followed by \"=\"");
                    return Token.ERROR;
                }
            case '!':
                nextChar();
                if (curChar == '=') {
                    nextChar();
                    return Token.NEQ;
                } else {
                    //error
                    this.Error("Scanner Error!"+"\"!\" should be followed by \"=\"");
                    return Token.ERROR;
                }
            case '>':
                nextChar();
                if (curChar == '=') {
                    nextChar();
                    return Token.GEQ;
                } else {
                    return Token.GRE;
                }
            case '<':
                nextChar();
                if (curChar == '=') {
                    nextChar();
                    return Token.LEQ;
                } else if(curChar == '-'){
                    nextChar();
                    return Token.BECOMETO;
                } else {
                    return Token.LSS;
                }
                // if punctuation(. , ; :)
            case '.':
                nextChar();
                return Token.PERIOD;
            case ',':
                nextChar();
                return Token.COMMA;
            case ';':
                nextChar();
                return Token.SEMICOMA;
            case ':':
                nextChar();
                return Token.COLON;
            // if block (, ), [, ], {, }
            case '(':
                nextChar();
                return Token.OPEN_PARENTHESIS;
            case ')':
                nextChar();
                return Token.CLOSE_PARENTHESIS;
            case '[':
                nextChar();
                return Token.OPEN_BRACKET;
            case ']':
                nextChar();
                return Token.CLOSE_BRACKET;
            case '{':
                nextChar();
                return Token.OPEN_BRACE;
            case '}':
                nextChar();
                return Token.CLOSE_BRACE;
        }

        return Token.ERROR;
    }

    /**
     * Skip space and comment
     * tab: \t;
     * carriage return: \r;
     * new line: \n;
     * space: \b or ' ';
     * in PL241, both # and / are used for comments
     * @throws IOException
     */
    public Token skipSpaceAndComment() throws IOException {
        while (curChar == '\t' || curChar == '\r' || curChar == '\n' || curChar == ' ' || curChar == '#' || curChar == '/') {
            if (curChar == '\t' || curChar == '\r' || curChar == '\n' || curChar == ' ') {
                nextChar();
            } else if (curChar == '#') {
                nextLine();
            } else if (curChar == '/') {
                nextChar();
                if (curChar == '/') {
                    nextLine();
                } else {
                    return Token.DIVIDE;
                }
            }
        }
        return null;
    }

    public Token getNumberToken() throws IOException {
        boolean isNumber = false;
        this.val = 0;
        while (curChar >= '0' && curChar <= '9') { // If digit, number
            isNumber = true;
            // update val
            this.val = 10 * this.val + curChar - '0';
            nextChar();
        }
        return isNumber ? Token.NUMBER : null;
    }

    public Token getLetterToken() throws IOException{
        boolean isLetter = false;
        StringBuilder sb = null;
        while (Character.isLetterOrDigit(curChar)) { // if letter or digit, ident or keyword
            // The first letter should be a letter, actually digit is already filtered when
            // checking number token
            if (!isLetter && Character.isDigit(curChar))
                return null; // TODO maybe throw an exception here is a better choice
            isLetter = true;
            if (sb == null){
                sb = new StringBuilder("");
                sb.append(curChar);
            }
            else
                sb.append(curChar);
            nextChar();
        }
        if (!isLetter) // no letter Token
            return null;

        String candidate = sb.toString();
        Token keywordToken = Token.buildToken(candidate);
        if (keywordToken != null) // found in keyword library, so it's keyword token
            return keywordToken;

        // otherwise, it's ident
        if (!preDefinedFunction.contains(candidate))
            preDefinedFunction.add(candidate);
        this.id = preDefinedFunction.indexOf(candidate);
        return Token.IDENTIFIER;
    }

    public int getLineNumber(){
        return fReader.getNumOfLine();
    }

    public void Error(String errMsg) {
        System.err.println("Scanner error: Syntax error at " + fReader.getNumOfLine() + ": " + errMsg);
    }
}
