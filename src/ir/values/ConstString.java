package ir.values;

import ir.types.IntegerType;
import ir.types.PointerType;
import ir.types.Type;

public class ConstString extends Const{
    private String value;
    private int length;


    public ConstString(String value) {
        super("\"" + value.replace("\n", "\\n") + "\"", new PointerType(IntegerType.i8));
        this.length = value.length() + 1;
        this.value = value.replace("\n", "\\0a") + "\\00";
    }

    public String getValue() {
        return value;
    }

    /**
     * 构建Mips代码时调用
     * 将llvm中的\0a转换为\n
     */
    public String getContent() {
        String t = value.replace("\\0a\\00", "\\n");
        t = t.replace("\\0a", "\\n");
        t = t.replace("\\00", "\\0");
//        System.out.println(t);
        return t;
    }

    public String toString() {
        return "[" + length + " x " + ((PointerType) getType()).getTargetType() + "] c\"" + value + "\"";
    }
}
