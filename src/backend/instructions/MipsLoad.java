package backend.instructions;

import backend.operands.MipsOperand;

public class MipsLoad extends MipsInstruction {
    public MipsLoad(MipsOperand dst, MipsOperand base, MipsOperand offset) {
        super(dst, base, offset);
    }

    public MipsOperand getBase() {
        return getSrc(1);
    }
    public MipsOperand getOffset() {
        return getSrc(2);
    }
    public void setOffset(MipsOperand offset) {
        setSrc(2, offset);
    }
    public String toString() {
        return "lw\t" + getDst() + ",\t" + getOffset() + "(" + getBase() + ")\n";
    }
}
