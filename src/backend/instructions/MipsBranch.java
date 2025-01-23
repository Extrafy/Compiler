package backend.instructions;

import backend.operands.MipsOperand;
import backend.parts.MipsBlock;

public class MipsBranch extends MipsInstruction {
    // 跳转时src比较的条件，或null代表无条件跳转
    private MipsCondType condType;
    //目的Mips块
    private MipsBlock target;

    // 无条件跳转指令
    public MipsBranch(MipsBlock target) {
        this.target = target;
        this.condType = null;
    }

    // 有条件跳转指令
    public MipsBranch(MipsCondType condType, MipsOperand src1, MipsOperand src2, MipsBlock target) {
        super(null, src1, src2);
        this.target = target;
        this.condType = condType;
    }

    public void setTarget(MipsBlock target) {
        this.target = target;
    }
    public MipsBlock getTarget() {
        return target;
    }
    public MipsCondType getCondType() {
        return condType;
    }
    public void setCondType(MipsCondType type) {
        this.condType = type;
    }

    @Override
    public String toString() {
        if (getSrc(1) == null) {
            return "j\t" + target.getName() + "\n";
        }
        else {
            return "b" + condType + "\t" + getSrc(1) + ",\t" + getSrc(2) + ",\t" + target.getName() + "\n";
        }
    }
}

