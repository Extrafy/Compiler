package ir.values.instructions.mem;

import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.Type;
import ir.values.BasicBlock;
import ir.values.instructions.Operator;

public class AllocaInst extends MemInst{
    private boolean isConst;
    public Type allocaType;

    public AllocaInst(BasicBlock block, boolean isConst, Type allocaType){
        super(new PointerType(allocaType), Operator.Alloca, block);
        this.setName("%" + REG_NUM++);
        this.isConst = isConst;
        this.allocaType = allocaType;
        // 处理数组
        if (allocaType instanceof ArrayType) {
            if (((ArrayType) allocaType).getArrayLength() == -1) {
                this.allocaType = new PointerType(((ArrayType) allocaType).getElementType());
                setType(new PointerType(this.allocaType));
            }
        }
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean isAConst) {
        isConst = isAConst;
    }

    public Type getAllocaType() {
        return allocaType;
    }

    public void setAllocaType(Type allocaType) {
        this.allocaType = allocaType;
    }

    @Override
    public String toString() {
        return this.getName() + " = alloca " + this.getAllocaType();
    }
}
