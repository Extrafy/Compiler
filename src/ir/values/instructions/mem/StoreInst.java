package ir.values.instructions.mem;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import ir.types.IntegerType;
import ir.types.Type;
import ir.values.BasicBlock;
import ir.values.ConstChar;
import ir.values.ConstInt;
import ir.values.Value;
import ir.values.instructions.ConvInst;
import ir.values.instructions.Operator;

public class StoreInst extends MemInst{

    public StoreInst(BasicBlock block, Value pointer, Value value) {
        super(value.getType(), Operator.Store, block);
        if (value instanceof ConstInt){
//            System.out.println(value.toString());
            if (((ConstInt)value).getIntType() == IntegerType.i8){
                try {
                    int number = Integer.parseInt(value.getName());
                    number = number & 0xff;
//                    System.out.println("转换后的整数为: " + number);
                    value.setName(Integer.toString(number));
                } catch (NumberFormatException e) {
                    System.out.println("字符串转换为整数失败: " + e.getMessage());
                }
            }
        }
        this.addOperand(value);
        this.addOperand(pointer);
    }

    public Value getValue() {
        return getOperands().get(0);
    }

    public Value getPointer() {
        return getOperands().get(1);
    }

    public String toString() {
        return "store " + getValue().getType() + " " + getValue().getName() + ", " + getPointer().getType() + " " + getPointer().getName();
    }

    public void buildMips() {
        MipsOperand src = MipsBuilder.buildOperand(getValue(), false, MipsBuildingContext.curIrFunction, getParent());
        MipsOperand base = MipsBuilder.buildOperand(getPointer(), false, MipsBuildingContext.curIrFunction, getParent());
        MipsOperand offset = MipsBuilder.buildImmOperand(0, true, MipsBuildingContext.curIrFunction, getParent());
        if (this.getValue().getType() == IntegerType.i8){
            Value temp = new ConstInt(255);
            MipsOperand src2;
            src2 = MipsBuilder.buildOperand(temp, true, MipsBuildingContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.AND, src, src, src2, getParent());
        }
//        System.out.println(this);
//        System.out.println(getValue());
//        System.out.println(getPointer());
        MipsBuilder.buildStore(src, base, offset, getParent());
    }

}
