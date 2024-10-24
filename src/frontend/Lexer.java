package frontend;

import config.Config;
import error.Error;
import error.ErrorType;
import error.HandleError;
import token.Token;
import token.TokenType;
import utils.InputOutput;

import java.util.*;

public class Lexer {
    private int curPosition;
    private int curLine;
    private String value;
    private long number;
    private TokenType tokenType;
    private List<Token> tokenList = new ArrayList<Token>();

    public List<Token> getTokenList(){
        return this.tokenList;
    }

    private static final HashMap<String, TokenType> reservedWords = new HashMap<String, TokenType>();

    static {
        reservedWords.put("main", TokenType.MAINTK);
        reservedWords.put("const", TokenType.CONSTTK);
        reservedWords.put("int", TokenType.INTTK);
        reservedWords.put("char", TokenType.CHARTK);
        reservedWords.put("break", TokenType.BREAKTK);
        reservedWords.put("continue", TokenType.CONTINUETK);
        reservedWords.put("if", TokenType.IFTK);
        reservedWords.put("else", TokenType.ELSETK);
        reservedWords.put("for", TokenType.FORTK);
        reservedWords.put("getint", TokenType.GETINTTK);
        reservedWords.put("getchar", TokenType.GETCHARTK);
        reservedWords.put("printf", TokenType.PRINTFTK);
        reservedWords.put("return", TokenType.RETURNTK);
        reservedWords.put("void", TokenType.VOIDTK);
    }

    private Lexer(){
        this.curLine = 1;
        this.curPosition = 0;
        this.number = 0;
        this.value = "";
        this.tokenType = TokenType.NONE;
    }

    private void resetting(){
        this.number = 0;
        this.value = "";
        this.tokenType = TokenType.NONE;
    }

    private static final Lexer instance = new Lexer();


    public static Lexer getInstance() {
        return instance;
    }

    public void analyse(String source){
        int maxLength = source.length();
        for(int i = 0; i < maxLength; i++){
            curPosition = i;
            char ch = source.charAt(i);
            char next = '\0';
            if(i + 1 < maxLength) next = source.charAt(i+1);

            if(ch == '\n') curLine++;

            else if (ch == '_' || Character.isLetter(ch)){
                for(int j = i; j < maxLength; j ++){
                    char t = source.charAt(j);
                    if(t == '_' || Character.isLetter(t) || Character.isDigit(t)){
                        value += t;
                    }
                    else {
                        i = j - 1;
                        break;
                    }
                }
                tokenType = reservedWords.getOrDefault(value, TokenType.IDENFR);
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(Character.isDigit(ch)){
                for (int j = i; j < maxLength; j++){
                    char t = source.charAt(j);
                    if(Character.isDigit(t)){
                        value += t;
                    }
                    else {
                        i = j - 1;
                        break;
                    }
                }
                tokenType = TokenType.INTCON;
                number = Long.parseLong(value);
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '\"'){
                value += ch;
                for (int j = i + 1; j < maxLength; j++){
                    char t = source.charAt(j);
                    if(t == '\\'){
                        value += t;
                        if (j + 1 < maxLength){
                            value += source.charAt(j + 1);
                        }
                        j++;
                    }
                    else if(t != '\"'){
                        value += t;
                    }
                    else {
                        i = j;
                        value += ch;
                        break;
                    }
                }
                tokenType = TokenType.STRCON;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '\''){
                value += ch;
                if(i + 1 < maxLength){
                    i++;
                    char t = source.charAt(i);
                    if(t != '\\'){
                        value += t;
                        if(i + 1 < maxLength){
                            i++;
                            value += ch;
                        }
                    }
                    else{
                        value += t;
                        if(i + 1 < maxLength){
                            i++;
                            char k = source.charAt(i);
                            Set<Character> charSet = Set.of('\"', '\'', '\\', 'a', 'b', 't', 'n', 'v', 'f', '0');
                            if(charSet.contains(k)){
                                value += k;
                                if(i + 1 < maxLength){
                                    i++;
                                    value += ch;
                                }
                            }
                            else{
                                System.out.println("字符常量不合法");
                            }
                        }
                    }
                }
                tokenType = TokenType.CHRCON;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '!'){
                value += ch;
                if(next != '='){
                    tokenType = TokenType.NOT;
                }
                else{
                    tokenType = TokenType.NEQ;
                    value += next;
                    i++;
                }
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '<'){
                value += ch;
                if(next != '='){
                    tokenType = TokenType.LSS;
                }
                else{
                    tokenType = TokenType.LEQ;
                    value += next;
                    i++;
                }
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '>'){
                value += ch;
                if(next != '='){
                    tokenType = TokenType.GRE;
                }
                else{
                    tokenType = TokenType.GEQ;
                    value += next;
                    i++;
                }
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '='){
                value += ch;
                if(next != '='){
                    tokenType = TokenType.ASSIGN;
                }
                else{
                    tokenType = TokenType.EQL;
                    value += next;
                    i++;
                }
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '&'){
                if(next == '&'){
                    value = "&&";
                    tokenType = TokenType.AND;
                    i++;
                    Token token = new Token(tokenType, value, curLine);
                    tokenList.add(token);
                    resetting();
                }
                else {
                    value = "&";
                    tokenType = TokenType.ERROR;
                    Token token = new Token(tokenType, value, curLine);
                    tokenList.add(token);
                    resetting();
                    HandleError.getInstance().addError(new Error(curLine, ErrorType.a));
                    Config.errorFlag = true;
                }
            }

            else if(ch == '|'){
                if(next == '|'){
                    value = "||";
                    tokenType = TokenType.OR;
                    i++;
                    Token token = new Token(tokenType, value, curLine);
                    tokenList.add(token);
                    resetting();
                }
                else {
                    value = "|";
                    tokenType = TokenType.ERROR;
                    Token token = new Token(tokenType, value, curLine);
                    tokenList.add(token);
                    resetting();
                    HandleError.getInstance().addError(new Error(curLine, ErrorType.a));
                    Config.errorFlag = true;
                }
            }

            else if(ch == '/'){
                if (next == '/'){
                    boolean flag = false; // 注释后面是否有回车
                    for(int j = i+2; j < maxLength; j++){
                        char t = source.charAt(j);
                        if(t == '\n'){
                            i = j;
                            curLine++;
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        i = maxLength - 1;
                    }
                }
                else if (next == '*'){
                    for(int j = i+2; j < maxLength; j++){
                        char t = source.charAt(j);
                        if(t == '\n') curLine++;
                        if(source.charAt(j) == '*' && j + 1 < maxLength && source.charAt(j+1) == '/'){
                            i = j+1;
                            break;
                        }
                    }
                }
                else {
                    value += ch;
                    tokenType = TokenType.DIV;
                    Token token = new Token(tokenType, value, curLine);
                    tokenList.add(token);
                    resetting();
                }
            }

            else if(ch == '+'){
                value += ch;
                tokenType = TokenType.PLUS;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '-'){
                value += ch;
                tokenType = TokenType.MINU;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '*'){
                value += ch;
                tokenType = TokenType.MULT;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '%'){
                value += ch;
                tokenType = TokenType.MOD;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == ';'){
                value += ch;
                tokenType = TokenType.SEMICN;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == ','){
                value += ch;
                tokenType = TokenType.COMMA;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '('){
                value += ch;
                tokenType = TokenType.LPARENT;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == ')'){
                value += ch;
                tokenType = TokenType.RPARENT;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '['){
                value += ch;
                tokenType = TokenType.LBRACK;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == ']'){
                value += ch;
                tokenType = TokenType.RBRACK;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '{'){
                value += ch;
                tokenType = TokenType.LBRACE;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '}'){
                value += ch;
                tokenType = TokenType.RBRACE;
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }
        }
    }

    public void printLexerAnswer(){
        for (Token token: tokenList){
            InputOutput.write(token.toString());
        }
    }
}
