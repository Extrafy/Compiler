package backend.instructions;

import backend.operands.MipsImm;
import backend.operands.MipsLabel;
import backend.operands.MipsOperand;

public class MipsMove extends MipsInstruction {
    public MipsMove(MipsOperand dst, MipsOperand src1) {
        super(dst, src1);
    }

    public void setSrc(MipsOperand src) {
        super.setSrc(1, src);
    }
    public String toString() {
        // 立即数:li
        if (getSrc(1) instanceof MipsImm) {
            return "li\t" + dst + ",\t" + getSrc(1) + "\n";
        }
        // 加载标签地址 la，处理全局变量
        else if (getSrc(1) instanceof MipsLabel) {
            return "la\t" + dst + ",\t" + getSrc(1) + "\n";
        }
        // 寄存器：move
        else {
            return "move\t" + dst + ",\t" + getSrc(1) + "\n";
        }
    }
}
