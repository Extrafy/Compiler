import config.Config;
import error.Error;
import error.HandleError;
import frontend.Lexer;
import frontend.Parser;
import symbol.SymbolTable;
import token.Token;
import utils.InputOutput;

import java.util.List;

public class Compiler {
    public static void main(String[] args) {
        String source = InputOutput.read(Config.inputPath);
        Lexer lexer = Lexer.getInstance();
        lexer.analyse(source);
        if(Config.lexerFlag){
            lexer.printLexerAnswer();
        }
        Parser parser = Parser.getInstance();
        parser.setTokenList(lexer.getTokenList());
        parser.analyse();
        if (Config.parserFlag){
            parser.printParseAnswer();
        }
        HandleError.getInstance().compUnitError(Parser.getInstance().getAst().compUnit);
        if (Config.symbolFlag){
            SymbolTable.getRootSymbolTable().printSymbols();
        }
        if (Config.errorFlag){
            HandleError handleError = HandleError.getInstance();
            handleError.printErrors();
        }
    }
}