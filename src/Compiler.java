import config.Config;
import error.Error;
import error.HandleError;
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
        if (Config.errorFlag){
            HandleError handleError = HandleError.getInstance();
            List<Error> errorList = handleError.getErrorList();
            for (Error error: errorList){
//                System.out.println(error.toString());
                InputOutput.writeError(error.toString());
            }
        }
    }
}