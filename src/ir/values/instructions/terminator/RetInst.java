package ir.values.instructions.terminator;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import ir.types.Type;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.instructions.Operator;

public class RetInst extends TerminatorInst{
    public RetInst(BasicBlock block) {
        super(VoidType.voidType, Operator.Ret, block);
    }

    public RetInst(BasicBlock block, Value ret) {
        super(ret.getType(), Operator.Ret, block);
        this.addOperand(ret);
    }

    public boolean isVoid(){
        return this.getOperands().isEmpty();
    }

    public String toString() {
        if (getOperands().size() == 1) {
            return "ret " + getOperands().get(0).getType() + " " + getOperands().get(0).getName();
        }
        else {
            return "ret void";
        }
    }

    public void buildMips() {
        Value returnValue = getOperands().get(0);
        // 带返回值，则存入v0
        if(returnValue != null){
            MipsOperand returnOperand = MipsBuilder.buildOperand(returnValue, true, MipsBuildingContext.curIrFunction, getParent());
            MipsBuilder.buildMove(MipsRealReg.V0, returnOperand, getParent());
        }
        // 之后进行弹栈以及返回操作，该操作位于MipsRet的toString()
        MipsBuilder.buildRet(MipsBuildingContext.curIrFunction, getParent());
    }
}
