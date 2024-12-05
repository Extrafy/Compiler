package ir.values;

import backend.parts.MipsGlobalVariable;
import backend.parts.MipsModule;
import ir.IRModule;
import ir.types.IntegerType;
import ir.types.PointerType;
import ir.types.Type;
import ir.values.instructions.ConstArray;

import java.util.ArrayList;

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

    public void buildMips(){
        MipsGlobalVariable mipsGlobalVariable = null;
        // 无初始值错误
        if(value == null){
            System.out.println("[buildMips] GlobalVariable：initValue == null");
        }
        // 未初始化的int数组
        else if(value instanceof ConstArray && ((ConstArray) value).allZero()){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), value.getType().getSize());
        }
        // 常量字符串
        else if(value instanceof ConstString){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), ((ConstString) value).getContent());
        }
        // int变量
        else if((value instanceof ConstInt)  && (((ConstInt)value).getIntType() == IntegerType.i32)){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), new ArrayList<>(){{
                add(((ConstInt) value).getValue());
            }});
        }
        // int数组
        else if((value instanceof ConstArray) && (((ConstArray) value).getElementType() == IntegerType.i32)){
            ArrayList<Integer> ints = new ArrayList<>();
            for (Value element : ((ConstArray) value).getArray()){
                ints.add(((ConstInt) element).getValue());
            }
            mipsGlobalVariable = new MipsGlobalVariable(getName(), ints);
        }
        // char变量
        else if(((value instanceof ConstInt)  && (((ConstInt)value).getIntType() == IntegerType.i8)) || (value instanceof ConstChar)){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), new ArrayList<>(){{
                add(((ConstInt) value).getValue());
            }}, true);
        }
        // char数组
        else if((value instanceof ConstArray) && (((ConstArray) value).getElementType() == IntegerType.i8)){
            ArrayList<Integer> ints = new ArrayList<>();
            for (Value element : ((ConstArray) value).getArray()){
                ints.add(((ConstInt) element).getValue());
            }
            mipsGlobalVariable = new MipsGlobalVariable(getName(), ints, true);
        }
        MipsModule.addGlobalVariable(mipsGlobalVariable);
    }
}
