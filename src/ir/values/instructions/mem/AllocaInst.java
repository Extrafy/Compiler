package ir.values.instructions.mem;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.parts.MipsFunction;
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

    public void buildMips() {
        MipsFunction curFunction = MipsBuildingContext.f(MipsBuildingContext.curIrFunction);
        // 在栈上已经分配出的空间
        int allocaedSize = curFunction.getAllocaSize();
        MipsOperand allocaedSizeOperand = MipsBuilder.buildImmOperand(allocaedSize, true, MipsBuildingContext.curIrFunction, getParent());
        // 记录 分配出指向类型那么多的空间
        int newSize = allocaType.getSize();
//        if (newSize % 4 != 0){  // 4字节对其(???mips)
//            newSize = (newSize/4 + 1) * 4;
//        }
        curFunction.addAllocaSize(newSize);
//        System.out.println("在函数分配空间" + curFunction.getName() + ", newSize:" + newSize+ ", name:" + this);

        // 向当前Alloca指令对应的Mips对象内，存入分配好的空间的首地址，即一开始的allocaedSize
        // 栈在一开始就已经分配好了空间，这里只需要向上生长即可
        MipsOperand dst = MipsBuilder.buildOperand(this, true, MipsBuildingContext.curIrFunction, getParent());
        MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, MipsRealReg.SP, allocaedSizeOperand, getParent());
    }
}
