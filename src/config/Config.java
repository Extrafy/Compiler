package config;

public class Config {
    public static String inputPath = "testfile.txt";
    public static String lexerOutputPath = "lexer.txt";
    public static String parserOutputPath = "parser.txt";
    public static String symbolOutputPath = "symbol.txt";
    public static String errorPath = "error.txt";
    public static String llvmPath = "llvm_ir.txt";
    public static String mipsPath = "mips.txt";

    public static boolean lexerFlag = true;
    public static boolean parserFlag = true;
    public static boolean symbolFlag = false;
    public static boolean errorFlag = false;
    public static boolean irFlag = false;
    public static boolean mipsFlag = false;

    // llvm是否开启str输出
    public static boolean chToStr = false;

    // 是否开启运算优化
    public static boolean addToMul = false; // 看看能否适当修改

    // 是否开启死代码，循环优化
    public static boolean openDeadCodeAndLoopOpt = false;
//    public static boolean openDeadCodeAndLoopOpt = true;

    // 是否开启Mem2Reg优化
    public static boolean openMem2RegOpt = false;
//    public static boolean openMem2RegOpt = true;

    // 是否开启窥孔优化
    public static boolean openPeepHoleOpt = false;
//    public static boolean openPeepHoleOpt = true;

    // 是否开启寄存器分配
    public static boolean openRegAllocOpt = false;
//    public static boolean openRegAllocOpt = true;
}
