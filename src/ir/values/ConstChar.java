package ir.values;

import ir.types.IntegerType;
import ir.types.Type;

import java.util.Objects;

public class ConstChar extends Const{
    private String value;

    public ConstChar(){
        super("\0", IntegerType.i8);
        this.value = "\0";
    }

    public ConstChar(String value) {
        super(value, IntegerType.i8);
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstChar temp = (ConstChar) o;
        return Objects.equals(value, temp.value);
    }

    public String toString() {
        return "i8 " + this.value;
    }
}
