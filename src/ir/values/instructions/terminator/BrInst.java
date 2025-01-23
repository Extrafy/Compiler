package ir.values.instructions.terminator;

import ir.types.IntegerType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.BuildFactory;
import ir.values.ConstInt;
import ir.values.Value;
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
}
