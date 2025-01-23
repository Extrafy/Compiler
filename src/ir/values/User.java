package ir.values;

import ir.types.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class User extends Value{
    private List<Value> operands; // User 使用的 Value 的列表， use-def 关系


    public User(String name, Type type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }

    public List<Value> getOperands() {
        return operands;
    }

    public void setOperands(List<Value> operands) {
        this.operands = operands;
    }

    public Value getOperand(int idx){
        return operands.get(idx);
    }

    public void setOperands(int pos, Value operand){
        if (pos >= operands.size()){
            return;
        }
        this.operands.set(pos, operand);
        if (operand != null){
            operand.addUse(new Use(this, operand, pos));
        }
    }

    public void addOperand(Value operand){
        this.operands.add(operand);
        if (operand != null){
            operand.addUse(new Use(this, operand, operands.size() - 1));
        }
    }

    public void removeUserFromOperands(){
        if (operands == null){
            return;
        }
        for (Value operand : operands){
            if (operand != null){
                operand.removeUseByUser(this);
            }
        }
    }

    public void removeOperandByIdx(HashSet<Integer> idx){
        removeUserFromOperands();
        List<Value> t = new ArrayList<>(operands);
        operands.clear();
        for (int i = 0; i < t.size(); i ++){
            //不在就把这个Value加回operands里
            if (!idx.contains(i)){
                t.get(i).addUse(new Use(this, t.get(i), operands.size()));
                this.operands.add(t.get(i));
            }
        }
    }

    // 把 operands 中的操作数替换成 value， 并更新 def-use 关系
    public void replaceOperands(int idx, Value value){
        Value operand = operands.get(idx);
        this.setOperands(idx, value);
        if (operand != null && !this.operands.contains(value)){
            operand.removeUseByUser(this);
        }
    }

    // 把oldOperand转换为newOperand
    public void replaceOperands(Value oldValue, Value newValue){
        oldValue.removeUseByUser(this);
        for (int i = 0 ; i < operands.size(); i++){
            if (operands.get(i) == oldValue){
                replaceOperands(i, newValue);
            }
        }
    }
}
