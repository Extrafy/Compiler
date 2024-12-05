package ir.types;

public class IntegerType implements Type{
    private final int bit;

    public static final IntegerType i1 = new IntegerType(1); // bool

    public static final IntegerType i8 = new IntegerType(8); // char

    public static final IntegerType i32 = new IntegerType(32); // integer

    private IntegerType(int bit){
        this.bit = bit;
    }

    public boolean isI1(){
        return this.bit == 1;
    }

    public boolean isI8(){
        return this.bit == 8;
    }

    public boolean isI32(){
        return this.bit == 32;
    }

    public String toString(){
        return "i" + Integer.toString(bit);
    }

    public int getSize() {
        return bit / 8;
    }
}
