package ir.values;

import ir.IRModule;
import ir.types.PointerType;
import ir.types.Type;
import ir.values.instructions.ConstArray;

public class GlobalVar extends User{
    private boolean isConst;
    private Value value;


    public GlobalVar(String name, Type type, boolean isConst, Value value) {
        super("@"+name, new PointerType(type));
        this.isConst = isConst;
        this.value = value;
        IRModule.getInstance().addGlobalVar(this);
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public boolean isInt() {
        return value instanceof ConstInt;
    }

    public boolean isString() {
        return value instanceof ConstString;
    }

    // ??? 还有单个字符

    public boolean isArray() {
        return value instanceof ConstArray;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append(this.getName()).append(" = ");
        if (isConst) {
            s.append("constant ");
        }
        else {
            s.append("global ");
        }
        if (value != null) {
            s.append(value);
        }
        return s.toString();
    }
}
