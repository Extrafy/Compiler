package frontend;

import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lexer {
    private int curPosition;
    private int curLine;
    private String value;
    private int number;
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
                number = Integer.parseInt(value);
                Token token = new Token(tokenType, value, curLine);
                tokenList.add(token);
                resetting();
            }

            else if(ch == '\"'){
                value += ch;
                for (int j = i + 1; j < maxLength; j++){
                    char t = source.charAt(j);
                    if(t != '\"'){
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
                tokenList.add((token));
                resetting();
            }
        }
    }

}
