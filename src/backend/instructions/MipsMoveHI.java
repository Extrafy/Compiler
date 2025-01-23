package backend.instructions;

import backend.operands.MipsOperand;

public class MipsMoveHI extends MipsInstruction {
    public enum MoveHIType {
        // 将一个通用寄存器的值移动到HI寄存器中
        MTHI,
        // 将HI寄存器中的值移动到一个通用寄存器中
        MFHI;
    }

    private MoveHIType type;

    public MipsMoveHI(MoveHIType type, MipsOperand dst, MipsOperand src) {
        super(type == MoveHIType.MTHI ? null : dst, type == MoveHIType.MFHI ? null : src);
        this.type = type;
    }

    public String toString() {
        if (type == MoveHIType.MTHI) {
            return "mthi\t" + getSrc(1) + "\n";
        }
        else {
            return "mfhi\t" + getDst() + "\n";
        }
    }
}

