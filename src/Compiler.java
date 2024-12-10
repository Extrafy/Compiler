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
            new FileWriter("llvm_ir.txt", false).close(); // 清空 llvm_ir.txt
            new FileWriter("mips.txt", false).close();    // 清空 mips.txt
        } catch (IOException e) {
            System.err.println("清空文件时出错：" + e.getMessage());
        }
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