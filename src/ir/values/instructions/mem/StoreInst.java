package ir.values.instructions.mem;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.operands.MipsOperand;
import ir.types.Type;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.instructions.Operator;

public class StoreInst extends MemInst{

    public StoreInst(BasicBlock block, Value pointer, Value value) {
        super(value.getType(), Operator.Store, block);
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
        MipsBuilder.buildStore(src, base, offset, getParent());
    }

}
