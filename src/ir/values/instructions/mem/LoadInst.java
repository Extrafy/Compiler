package ir.values.instructions.mem;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.operands.MipsOperand;
import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.Type;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.instructions.Operator;

public class LoadInst extends MemInst{

    public LoadInst(BasicBlock block, Value pointer) {
        super(((PointerType) pointer.getType()).getTargetType(), Operator.Load, block);
        this.setName("%" + REG_NUM++);
        if (getType() instanceof ArrayType){
            setType(new PointerType(((ArrayType) getType()).getElementType()));
        }
        this.addOperand(pointer);
    }

    public Value getPointer() {
        return getOperands().get(0);
    }

    public Value getIndex() {
        return getOperands().get(1);
    }

    public String toString() {
        return getName() + " = load " + getType() + ", " + getPointer().getType() + " " + getPointer().getName();
    }

    public void buildMips() {
        MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());
        MipsOperand base = MipsBuilder.buildOperand(getOperands().get(0), false, MipsBuildingContext.curIrFunction, getParent());
        MipsOperand offset = MipsBuilder.buildImmOperand(0, true, MipsBuildingContext.curIrFunction, getParent());
        MipsBuilder.buildLoad(dst, base, offset, getParent());
    }
}
