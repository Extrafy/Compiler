import backend.MipsBuilder;
import config.Config;
import error.HandleError;
import frontend.Lexer;
import frontend.Parser;
import ir.IRModule;
import ir.LLVMGenerator;
import ir.opt.*;
import symbol.SymbolTable;
import utils.InputOutput;

import java.io.FileWriter;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {
            new FileWriter("lexer.txt", false).close(); // 清空 lexer.txt
            new FileWriter("parser.txt", false).close(); // 清空 parser.txt
            new FileWriter("symbol.txt", false).close(); // 清空 symbol.txt
            new FileWriter("error.txt", false).close(); // 清空 error.txt
            new FileWriter("llvm_ir.txt", false).close(); // 清空 llvm_ir.txt
            new FileWriter("mips.txt", false).close();    // 清空 mips.txt
        } catch (IOException e) {
            System.err.println("清空文件时出错：" + e.getMessage());
        }
        String source = InputOutput.read(Config.inputPath);
        if(Config.lexerFlag){
            Lexer lexer = Lexer.getInstance();
            lexer.analyse(source);
            lexer.printLexerAnswer();
        }
        if (Config.parserFlag){
            Parser parser = Parser.getInstance();
            parser.setTokenList(Lexer.getInstance().getTokenList());
            parser.analyse();
            parser.printParseAnswer();
        }
        if (Config.symbolFlag){
            HandleError.getInstance().compUnitError(Parser.getInstance().getAst().compUnit);
            SymbolTable.getRootSymbolTable().printSymbols();
        }
        if (Config.errorFlag){
            HandleError handleError = HandleError.getInstance();
            handleError.printErrors();
        }
        if (!HandleError.getInstance().getErrorList().isEmpty()){
            return;
        }
        if (Config.irFlag){
            LLVMGenerator.getInstance().process();
            InputOutput.writeLlvmIr(IRModule.getInstance().toString());
        }
        if (Config.mipsFlag){
            MipsBuilder mipsBuilder = new MipsBuilder(IRModule.getInstance());
            // 生成目标代码
            mipsBuilder.process();
            // 输出目标代码
            mipsBuilder.outputResult();
        }
    }
}