package ir.values.instructions.terminator;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.instructions.MipsCondType;
import backend.operands.MipsOperand;
import backend.parts.MipsBlock;
import ir.types.IntegerType;
import ir.types.VoidType;
import ir.values.*;
import ir.values.instructions.BinaryInst;
import ir.values.instructions.Operator;

public class BrInst extends TerminatorInst{

    public BrInst(BasicBlock basicBlock, BasicBlock trueBlock) {
        super(VoidType.voidType, Operator.Br, basicBlock);

        this.addOperand(trueBlock);

        if (basicBlock != null) {
            if (basicBlock.getInstructions().getEnd() == null || (!(basicBlock.getInstructions().getEnd().getValue() instanceof BrInst) && !(basicBlock.getInstructions().getEnd().getValue() instanceof RetInst))) {
                trueBlock.addPredecessor(basicBlock);
                basicBlock.addSuccessor(trueBlock);
            }
        }
    }

    public BrInst(BasicBlock basicBlock, Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        super(VoidType.voidType, Operator.Br, basicBlock);

        Value temp = cond;
        if (!(cond.getType() instanceof IntegerType && ((IntegerType) cond.getType()).isI1())) {
            temp = BuildFactory.getInstance().buildBinary(basicBlock, Operator.Ne, cond, new ConstInt(0)); // ???
        }
        this.addOperand(temp);
        this.addOperand(trueBlock);
        this.addOperand(falseBlock);
        // 添加前驱后继
        if (basicBlock.getInstructions().getEnd() == null || (!(basicBlock.getInstructions().getEnd().getValue() instanceof BrInst) && !(basicBlock.getInstructions().getEnd().getValue() instanceof RetInst))) {
            trueBlock.addPredecessor(basicBlock);
            falseBlock.addPredecessor(basicBlock);
            basicBlock.addSuccessor(trueBlock);
            basicBlock.addSuccessor(falseBlock);
        }
    }

    public Value getTarget() {
        return this.getOperand(0);
    }

    public boolean isCondBr() {
        return this.getOperands().size() == 3;
    }

    public Value getCond() {
        if (isCondBr()) {
            return this.getOperand(0);
        }
        else {
            return null;
        }
    }

    public BasicBlock getTrueBlockLabel() {
        if (isCondBr()) {
            return (BasicBlock) this.getOperand(1);
        }
        else {
            return (BasicBlock) this.getOperand(0);
        }
    }

    public BasicBlock getFalseBlockLabel() {
        if (isCondBr()) {
            return (BasicBlock) this.getOperand(2);
        }
        else {
            return null;
        }
    }

    public String toString() {
        if (this.getOperands().size() == 1) {
            return "br label %" + this.getOperands().get(0).getName();
        }
        else {
            return "br " + this.getOperands().get(0).getType() + " " + this.getOperands().get(0).getName() + ", label %" + this.getOperands().get(1).getName() + ", label %" + this.getOperands().get(2).getName();
        }
    }

    public void buildMips() {
        // 将关于跳转的块 转换为mips块
        MipsBlock curBlock = MipsBuildingContext.b(getParent()); // 当前块
        // 有条件跳转
        if(isCondBr()){
            if(this.getOperands().get(0) instanceof ConstInt || this.getOperands().get(0) instanceof ConstChar){
                System.out.println("[Br] 错误：条件为ConstInt");
            }
            // 将关于跳转的块 转换为mips块
            MipsBlock trueBlock = MipsBuildingContext.b((BasicBlock) this.getOperands().get(1));      // 真跳转块
            MipsBlock falseBlock = MipsBuildingContext.b((BasicBlock) this.getOperands().get(2));     // 假跳转块

            BinaryInst condition = (BinaryInst) this.getOperands().get(0);   // ir跳转条件类
            // 获得具体的跳转条件
            MipsCondType type = MipsCondType.IrCondType2MipsCondType(condition.getOperator());
            // 获得两个比较数
//            System.out.println(condition.getOperand(0) + " " + condition.getOperand(1));
            MipsOperand src1 = MipsBuilder.buildOperand(condition.getOperand(0), false, MipsBuildingContext.curIrFunction, getParent());
            MipsOperand src2 = MipsBuilder.buildOperand(condition.getOperand(1), true, MipsBuildingContext.curIrFunction, getParent());
//            System.out.println(src1.toString() + src2.toString());
            // 将trueBlock设置为跳转地址
            MipsBuilder.buildBranch(type, src1, src2, trueBlock, getParent());
            // 登记后续块
            curBlock.setTrueSucBlock(trueBlock);
            curBlock.setFalseSucBlock(falseBlock);
        }
        // 无条件跳转指令
        else{
            MipsBlock targetBlock = MipsBuildingContext.b((BasicBlock) this.getOperands().get(0));
            MipsBuilder.buildBranch(targetBlock, getParent());
            // 登记后继块
            curBlock.setTrueSucBlock(targetBlock);
        }
    }
}
