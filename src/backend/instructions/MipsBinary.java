package backend.instructions;

import backend.operands.MipsImm;
import backend.operands.MipsOperand;

public class MipsBinary extends MipsInstruction {
    public enum BinaryType {
        SUBU("subu"),
        ADDU("addu"),
        MUL("mul"),
        DIV("div"),
        XOR("xor"),
        SLTU("sltu"),
        SLT("slt"),
        SMMUL("smmul"),
        AND("and"),
        SMMADD("smmadd");

        public String name;

        BinaryType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public BinaryType getType() {
        return type;
    }

    private final BinaryType type;

    public MipsBinary(BinaryType type, MipsOperand dst, MipsOperand src1, MipsOperand src2) {
        super(dst, src1, src2);
        this.type = type;
    }

    public String toString() {
        // 根据第二操作数是否为立即数，指令也不同
        // 立即数，选取相应带立即数i的指令
        if (getSrc(2) instanceof MipsImm) {
            String instr;
            switch (type) {
                case ADDU -> instr = "addiu";
                case SUBU -> instr = "subiu";
                case SLTU -> instr = "sltiu";
                default -> instr = type + "i";
            }
            return instr + "\t" + dst + ",\t" + getSrc(1) + ",\t" + getSrc(2) + "\n";
        }
        // 非立即数
        switch (type) {
            case SMMUL -> {
                // 	(HI, LO) ← src1 × src2
                //  dst ← HI
                return "mult\t" + getSrc(1) + ",\t" + getSrc(2) + "\n\t" +
                        "mfhi\t" + dst + "\n";
            }
            case DIV -> {
                //  (HI, LO) ← rs / rt
                //   rd ← LO
                return "div\t" + getSrc(1) + ",\t" + getSrc(2) + "\n\t" +
                        "mflo\t" + dst + "\n";
            }
            case SMMADD -> {
                // {HI, LO}<-{HI, LO}+ rs x rt
                // dst ← HI
                return "madd\t" + getSrc(1) + ",\t" + getSrc(2) + "\n\t" +
                        "mfhi\t" + dst + "\n";
            }
            default -> {
                return type + "\t" + dst + ",\t" + getSrc(1) + ",\t" + getSrc(2) + "\n";
            }
        }
    }
}

