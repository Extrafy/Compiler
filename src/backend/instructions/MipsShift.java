package backend.instructions;

import backend.operands.MipsOperand;

public class MipsShift extends MipsInstruction {
    public enum ShiftType {
        // 算数右移
        SRA,
        // 逻辑左移
        SLL,
        // 逻辑右移
        SRL
    }
    private final ShiftType type;
    private final int shift;

    public MipsShift(ShiftType type, MipsOperand dst, MipsOperand src, int shift) {
        super(dst, src);
        this.type = type;
        this.shift = shift;
    }

    public String toString() {
        String instr = "sll";
        if(type.equals(ShiftType.SRA)){
            instr = "sra";
        }
        else if(type.equals(ShiftType.SRL)){
            instr = "srl";
        }
        return instr + "\t" + getDst() + ",\t" + getSrc(1) + ",\t" + shift + "\n";
    }
}
