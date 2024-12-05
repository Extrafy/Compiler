package ir.values.instructions.mem;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import ir.types.ArrayType;
import ir.types.IntegerType;
import ir.types.PointerType;
import ir.types.Type;
import ir.values.BasicBlock;
import ir.values.ConstInt;
import ir.values.GlobalVar;
import ir.values.Value;
import ir.values.instructions.Operator;

import java.util.ArrayList;
import java.util.List;

public class GEPInst extends MemInst{
    private Type elementType;
    private Value target;

    public GEPInst(BasicBlock block, Value pointer, List<Value> indices) {
        super(new PointerType(getElementType(pointer, indices)), Operator.GEP, block);
        this.setName("%" + REG_NUM++);
        if (pointer instanceof GEPInst) {
            target = ((GEPInst) pointer).target;
        }
        else if (pointer instanceof AllocaInst) {
            target = pointer;
        }
        else if (pointer instanceof GlobalVar) {
            target = pointer;
        }
        this.addOperand(pointer);
        for (Value value : indices) {
            this.addOperand(value);
        }
        this.elementType = getElementType(pointer, indices);
    }

    public GEPInst(BasicBlock block, Value pointer, int offset){
        this(block, pointer, ((ArrayType) ((PointerType) pointer.getType()).getTargetType()).offsetToIndex(offset));
    }

    public Value getPointer() {
        return getOperands().get(0);
    }

    private static Type getElementType(Value pointer, List<Value> indices) {
        // ???
        Type type = pointer.getType();
        for (Value ignored : indices) {
            if (type instanceof ArrayType) {
                type = ((ArrayType) type).getElementType();
            }
            else if (type instanceof PointerType) {
                type = ((PointerType) type).getTargetType();
            }
            else {
                break;
            }
        }
        return type;
    }

    public List<Integer> getGEPIndex() {
        List<Integer> index = new ArrayList<>();
        for (int i = 1; i < getOperands().size(); i++) {
            index.add(((ConstInt) getOperand(i)).getValue());
        }
        return index;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getName()).append(" = getelementptr ");
        // String 需要加 inbounds
        if (getPointer().getType() instanceof PointerType && ((PointerType) getPointer().getType()).isString()) {
            s.append("inbounds ");
        }
        s.append(((PointerType) getPointer().getType()).getTargetType()).append(", ");
        for (int i = 0; i < getOperands().size(); i++) {
            if (i == 0) {
                s.append(getPointer().getType()).append(" ").append(getPointer().getName());
            }
            else {
                s.append(", ").append(getOperands().get(i).getType()).append(" ").append(getOperands().get(i).getName());
            }
        }
        return s.toString();
    }

    public void buildMips() {
        // 本维基地址
        Value irBase = getOperands().get(0);
        // 本维偏移
        Value irOffset1 = getOperands().get(1);

        // 获得数组的基地址的MipsOp
        MipsOperand base = MipsBuilder.buildOperand(irBase, false, MipsBuildingContext.curIrFunction, getParent());
        // 本value的MipsOp
        MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());

        // 根据操作数个数（降维次数）来讨论
        int opNumber = getOperands().size();
        // 单参数本维寻址，其类型与原指针类型相同
        if (opNumber == 2) {
            // 本维偏移
            handleDim(dst, dst, base, irOffset1, ((PointerType) getPointer().getType()).getTargetType(), true);
        }
        // 指向数组
        else if (opNumber == 3) {
            // 获得数组元素类型/偏移Value
            Value irOffset2 = getOperands().get(2);
            Type elementType = ((ArrayType) ((PointerType) getPointer().getType()).getTargetType()).getElementType();

            // 本维偏移
            handleDim(dst, dst, base, irOffset1, ((PointerType) getPointer().getType()).getTargetType(), false);
            // 低一维偏移
            handleDim(dst, MipsRealReg.AT, dst, irOffset2, elementType, false);
        }
    }

    /**
     * 计算某一维的偏移
     * @param dst           要将结果存入的寄存器
     * @param mid           中转寄存器
     * @param base          存有基地址的寄存器
     * @param irOffset      Offset的irValue
     * @param elementType   该维基本元素的irType
     * @param dim1Opt       是否为opNumber == 2的场合
     */
    private void handleDim(MipsOperand dst, MipsOperand mid, MipsOperand base, Value irOffset, Type elementType, boolean dim1Opt){
        int elementSize = elementType.getSize();

        // 为常数
        if (irOffset instanceof ConstInt) {
            int offsetIndex = ((ConstInt) irOffset).getValue();
            // 低一维偏移的具体值
            int totalOffset = elementSize * offsetIndex;
            // 不存在偏移，直接将this映射到base的op即可
            if (dim1Opt && totalOffset == 0){
                MipsBuildingContext.addOperandMapping(this, base);
            }
            else{
                MipsOperand totalOffsetOperand = MipsBuilder.buildImmOperand(totalOffset, true, MipsBuildingContext.curIrFunction, getParent());
                // dst = dst + offset
                MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, base, totalOffsetOperand, getParent());
            }
        }
        // 非常数
        else {
            // 利用mid寄存器周转
            // mid = offset = elementSize * offset
            MipsBuilder.buildOptMul(mid, irOffset, new ConstInt(elementSize, IntegerType.i32), MipsBuildingContext.curIrFunction, getParent());
            // dst = base + mid
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, base, mid, getParent());
        }
    }
}
