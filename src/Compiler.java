import config.Config;
import frontend.Lexer;
import token.Token;
import utils.InputOutput;

import java.util.List;

public class Compiler {
    public static void main(String[] args) {
        String source = InputOutput.read(Config.inputPath);
        Lexer lexer = Lexer.getInstance();
        lexer.analyse(source);
        List<Token> tokenList =  lexer.getTokenList();
        for (Token token: tokenList){
            InputOutput.write(token.toString());
        }
    }
}