package token;

public class Token {
    private TokenType tokenType;
    private String value;
    private int line;

    public Token(TokenType tokenType, String value, int line){
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return tokenType.toString() + ' ' + value;
//        return tokenType.toString() + ' ' + value + ' ' + line;
    }
}
