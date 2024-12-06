package ir.values.instructions;

import backend.MipsBuildingContext;
import ir.types.IntegerType;
import ir.types.PointerType;
import ir.types.Type;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.ConstChar;
import ir.values.ConstInt;
import ir.values.Value;
import ir.values.instructions.mem.LoadInst;
import ir.values.instructions.terminator.CallInst;

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
                // ???mips 这里i8 to i1可能有问题
//                System.out.println("zext: " + this.getOperands());
                Value i1 = this.getOperands().get(0);
//                System.out.println(i1.toString());
                if(i1 instanceof BinaryInst && ((BinaryInst) i1).isCond()){
                    ((BinaryInst) i1).buildMips1();
                    // 将Zext结果与i1的解析结果页进行绑定
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                }
                else if (i1 instanceof LoadInst && i1.getType() == IntegerType.i8){
//                    ((LoadInst) i1).buildMips();
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                }
                else if (i1 instanceof BinaryInst){
//                    ((BinaryInst) i1).buildMips(); ???mips
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                }
                else if (i1 instanceof ConstChar){
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                }
                else {
                    System.out.println("[Zext]出错");
                }
            }
            case Trunc -> {
//                System.out.println("trunc: " + this.getOperands());
                Value i1 = this.getOperands().get(0);
                if (i1 instanceof BinaryInst){
                    if(((BinaryInst) i1).isCond()){
                        ((BinaryInst) i1).buildMips1();
                    }
                    else ((BinaryInst) i1).buildMips();
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                }
                else if (i1 instanceof LoadInst && i1.getType() == IntegerType.i32){
//                    ((LoadInst) i1).buildMips();
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                }
                else if (i1 instanceof CallInst){
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                }
                else if (i1 instanceof ConstInt && ((ConstInt)i1).getIntType() == IntegerType.i32){
                    MipsBuildingContext.addOperandMapping(this, MipsBuildingContext.op(i1));
                }
                else {
                    System.out.println("[Trunc]出错");
                }
            }
        }
    }
}
