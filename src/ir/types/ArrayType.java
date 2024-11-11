package ir.types;

import ir.values.ConstInt;
import ir.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ArrayType implements Type{
    private final Type elementType;
    private final int arrayLength;

    public ArrayType(Type elementType) {
        this.elementType = elementType;
        this.arrayLength = 0;
    }

    public ArrayType(Type elementType, int arrayLength) {
        this.elementType = elementType;
        this.arrayLength = arrayLength;
    }

    public Type getElementType(){
        return elementType;
    }

    public boolean isIntegerArray(){
        return elementType instanceof IntegerType && ((IntegerType) elementType).isI32();
    }

    public boolean isCharArray(){
        return elementType instanceof IntegerType && ((IntegerType) elementType).isI8();
    }

    public int getArrayLength(){
        return arrayLength;
    }

    public int getCapacity(){
        return arrayLength;
    }

    public List<Value> offsetToIndex(int offset) {
        List<Value> index = new ArrayList<>();
        Type type = this;
        while (type instanceof ArrayType) {
            index.add(new ConstInt(offset / ((ArrayType) type).getCapacity()));
            offset %= ((ArrayType) type).getCapacity();
            type = ((ArrayType) type).getElementType();
        }
        index.add(new ConstInt(offset));
        return index;
    }

    public int indexToOffset(List<Integer> index) {
        int offset = 0, i = 0;
        Type type = this;
        offset += index.get(i++) * ((ArrayType) type).getCapacity();
        while (type instanceof ArrayType) {
            type = ((ArrayType) type).getElementType();
            if (type instanceof ArrayType) {
                offset += index.get(i++) * ((ArrayType) type).getCapacity();
            } else {
                offset += index.get(i++);
            }
        }
        return offset;
    }

    public String toString(){
        return "[" + arrayLength + " x " + elementType.toString() + "]";
    }

}
