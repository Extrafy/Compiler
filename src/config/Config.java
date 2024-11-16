package config;

public class Config {
    public static String inputPath = "testfile.txt";
    public static String outputPath = "symbol.txt";
    public static String errorPath = "error.txt";
    public static String llvmPath = "llvm_ir.txt";

    public static boolean lexerFlag = false;
    public static boolean parserFlag = false;
    public static boolean symbolFlag = false;
    public static boolean errorFlag = false;
    public static boolean irFlag = true;

    public static boolean chToStr = true;
    public static boolean addToMul = true;
}
