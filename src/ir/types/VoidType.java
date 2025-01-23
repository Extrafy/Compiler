package ir.types;

public class VoidType implements Type{
    public static final VoidType voidType = new VoidType();

    public String toString(){
        return "void";
    }

    public int getSize() {
        System.out.println("[VoidTypeSize] 非法获取Void类型的Size！");
        return 0;
    }
}
