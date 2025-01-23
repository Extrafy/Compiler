package ir.values;

import ir.types.IntegerType;
import ir.types.Type;

import java.util.Objects;

public class ConstInt extends Const{
    private int value;
    private Type type = IntegerType.i32;
    public static ConstInt zero = new ConstInt(0);
    public static ConstInt charZero = new ConstInt(0, IntegerType.i8);
    public ConstInt(){
        super("", IntegerType.i32);
        this.value = 0;
    }

    public ConstInt(int value){
        super(String.valueOf(value), IntegerType.i32);
        this.value = value;
    }

    public ConstInt(int value, Type type){
        super(String.valueOf(value),type);
        this.value = value;
        this.type = type;
    }

    public ConstInt(int value, boolean isI1){  // ???可能需要加i8
        super(String.valueOf(value), IntegerType.i1);
        this.value = value;
        this.type = IntegerType.i1;
    }

    public int getValue(){
        return value;
    }

    public Type getIntType() {
        return type;
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
        return this.type.toString() + " " + this.value;
    }
}
