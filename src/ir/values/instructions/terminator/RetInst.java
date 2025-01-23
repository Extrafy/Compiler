package ir.values.instructions.terminator;

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
}
