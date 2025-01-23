package backend.instructions;

import backend.operands.MipsOperand;

public class MipsCompare extends MipsInstruction{
    // 比较的类型
    private MipsCondType type;

    public MipsCompare(MipsCondType type, MipsOperand dst, MipsOperand src1, MipsOperand src2) {
        super(dst, src1, src2);
        this.type = type;
    }

    @Override
    public String toString() {
        return "s" + type + "\t" + dst + ",\t" + getSrc(1) + ",\t" + getSrc(2) + "\n";
    }
}
