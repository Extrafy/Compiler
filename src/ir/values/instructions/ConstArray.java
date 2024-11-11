package ir.values.instructions;

import ir.types.ArrayType;
import ir.types.Type;
import ir.values.Const;
import ir.values.ConstInt;
import ir.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ConstArray extends Const {
    private Type elementType;
    private List<Value> array;
    private int capacity;

    private boolean isInit = false;

    public ConstArray(Type type, Type elementType, int capacity){
        super("", type);
        this.elementType = elementType;
        this.array = new ArrayList<>();
        this.capacity = capacity;
        for (int i = 0; i < ((ArrayType) type).getArrayLength(); i++){
            array.add(ConstInt.zero);
        }
    }

    public Type getElementType() {
        return elementType;
    }

    public void setElementType(Type elementType) {
        this.elementType = elementType;
    }

    public List<Value> getArray() {
        return array;
    }

    public void setArray(List<Value> array) {
        this.array = array;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isInit(){
        return isInit || !allZero();
    }

    public void setInit(boolean init){
        this.isInit = init;
    }

    public List<Value> get1DArray() {  // ??? 这个感觉可以简化
        List<Value> result = new ArrayList<>();
        for (Value value : array) {
            if (value instanceof ConstArray) {
                result.addAll(((ConstArray) value).get1DArray());
            }
            else {
                result.add(value);
            }
        }
        return result;
    }

    public void storeValue(int offset, Value value) {
        // recursion
        if (elementType instanceof ArrayType) {  // ??? 这个感觉可以简化
            ((ConstArray) (array.get(offset / ((ArrayType) elementType).getCapacity()))).storeValue(offset % ((ArrayType) elementType).getCapacity(), value);
        }
        else {
            array.set(offset, value);
        }
    }

    public boolean allZero() {
        for (Value value : array) {
            if (value instanceof ConstInt) {
                if (((ConstInt) value).getValue() != 0) {
                    return false;
                }
            }
            else {   // ??? 这个感觉可以简化
                if (!((ConstArray) value).allZero()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        if (allZero()) {
            return this.getType().toString() + " " + "zeroinitializer";
        }
        else {
            StringBuilder s = new StringBuilder();
            s.append(this.getType().toString()).append(" ").append("[");
            for (int i = 0; i < array.size(); i++) {
                if (i != 0) {
                    s.append(", ");
                }
                s.append(array.get(i).toString());
            }
            s.append("]");
            return s.toString();
        }
    }

}
