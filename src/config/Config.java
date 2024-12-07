package config;

public class Config {
    public static String inputPath = "testfile.txt";
    public static String outputPath = "symbol.txt";
    public static String errorPath = "error.txt";
    public static String llvmPath = "llvm_ir.txt";
    public static String mipsPath = "mips.txt";

    public static boolean lexerFlag = false;
    public static boolean parserFlag = false;
    public static boolean symbolFlag = false;
    public static boolean errorFlag = false;
    public static boolean irFlag = true;
    public static boolean mipsFlag = true;

    public static boolean chToStr = true;
    public static boolean addToMul = false;

    // 是否开启Mem2Reg优化
    public static boolean openMem2RegOpt = false;
//    public static boolean openMem2RegOpt = true;

    // 是否开启窥孔优化
//    public static boolean openPeepHoleOpt = false;
    public static boolean openPeepHoleOpt = true;

    // 是否开启寄存器分配
//    public static boolean openRegAllocOpt = false;
    public static boolean openRegAllocOpt = true;
}
