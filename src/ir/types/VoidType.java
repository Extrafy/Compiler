package ir.types;

public class VoidType implements Type{
    public static final VoidType voidType = new VoidType();

    public String toString(){
        return "void";
    }
}
