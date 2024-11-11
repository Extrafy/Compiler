package ir.values.instructions;

import ir.types.IntegerType;
import ir.types.PointerType;
import ir.types.Type;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;

public class ConvInst extends Instruction{

    public ConvInst(BasicBlock block, Operator operator, Value value) {
        super(VoidType.voidType, operator, block);
        this.setName("%" + REG_NUM++);
        if (operator == Operator.Zext){
            setType(IntegerType.i32);
        }
        else if (operator == Operator.Trunc){
            setType(IntegerType.i8);
        }
        else if (operator == Operator.Bitcast){
            setType(new PointerType(IntegerType.i32));
        }
        addOperand(value);
    }

    public String toString() {  // ??? int 和 char 互转的时候有问题
        if (getOperator() == Operator.Bitcast) {
            return getName() + " = bitcast " + getOperands().get(0).getType() + getOperands().get(0).getName() + " to i32*";
        }
        else if (getOperator() == Operator.Zext) {
            return getName() + " = zext i1 " + getOperands().get(0).getName() + " to i32";
        }
        else if (getOperator() == Operator.Trunc) {
            return getName() + " = trunc i32" + getOperands().get(0).getName() + " to i1";
        }
        else {
            return null;
        }
    }
}
