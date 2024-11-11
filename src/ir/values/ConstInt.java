package ir.values;

import ir.types.IntegerType;

import java.util.Objects;

public class ConstInt extends Const{
    private int value;
    public static ConstInt zero = new ConstInt(0);
    public ConstInt(){
        super("", IntegerType.i32);
        this.value = 0;
    }

    public ConstInt(int value){
        super(String.valueOf(value), IntegerType.i32);
        this.value = value;
    }

    public ConstInt(int value, boolean isI1){  // ???可能需要加i8
        super(String.valueOf(value), IntegerType.i1);
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstInt temp = (ConstInt) o;
        return value == temp.value;
    }

    public int hashCode() {
        return Objects.hash(value);
    }

    public String toString() {
        return "i32 " + this.value;
    }
}
