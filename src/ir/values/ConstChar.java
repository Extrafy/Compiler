package ir.values;

import ir.types.IntegerType;
import ir.types.Type;

import java.util.Objects;

public class ConstChar extends Const{
    private int value;

    public ConstChar(){
        super("", IntegerType.i8);
        this.value = 0;
    }

    public ConstChar(int value) {
        super(String.valueOf(value), IntegerType.i8);
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstChar temp = (ConstChar) o;
        return value == temp.value;
    }

    public String toString() {
        return "i8 " + this.value;
    }
}
