package ir.values.instructions;

import backend.MipsBuildingContext;
import ir.types.IntegerType;
import ir.types.PointerType;
import ir.types.Type;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;

public class ConvInst extends Instruction{

    private Type from;
    private Type to;

    public Type getFrom() {
        return from;
    }

    public Type getTo() {
        return to;
    }

    public ConvInst(BasicBlock block, Operator operator, Value value, Type from, Type to) {
        super(VoidType.voidType, operator, block);
        this.setName("%" + REG_NUM++);
        this.from = from;
        this.to = to;
        if (operator == Operator.Zext){
            if (((IntegerType) to).isI8()){
                setType(IntegerType.i8);
            }
            else if (((IntegerType) to).isI32()){
                setType(IntegerType.i32);
            }
        }
        else if (operator == Operator.Trunc){
            if (((IntegerType) to).isI8()){
                setType(IntegerType.i8);
            }
            else if (((IntegerType) to).isI32()){
                setType(IntegerType.i32);
            }
        }
        else if (operator == Operator.Bitcast){
            if (((IntegerType) from).isI8()){
                setType(new PointerType(IntegerType.i8));
            }
            else if (((IntegerType) from).isI32()){
                setType(new PointerType(IntegerType.i32));
            }
        }
        addOperand(value);
    }

    public String toString() {  // ??? int 和 char 互转的时候有问题
        if (getOperator() == Operator.Bitcast) {
            return getName() + " = bitcast " + getOperands().get(0).getType() + getOperands().get(0).getName() + " to " + getType();
        }
        else if (getOperator() == Operator.Zext) {
            return getName() + " = zext "+ getFrom() + " " + getOperands().get(0).getName() + " to " + getTo();
        }
        else if (getOperator() == Operator.Trunc) {
            return getName() + " = trunc " + getFrom() + " " + getOperands().get(0).getName() + " to " + getTo();
        }
        else {
            return null;
        }
    }

    public void buildMips() {
        switch (this.getOperator()){
            case Zext -> {
                Value i1 = this.getOperands().get(0);
                if(i1 instanceof BinaryInst && ((BinaryInst) i1).isCond()){
                    ((BinaryInst) i1).buildMips1();
                    // 将Zext结果与i1的解析结果页进行绑定
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                } else {
                    System.out.println("[Zext]操作数i1不为Icmp类型");
                }
            }
        }
    }
}
