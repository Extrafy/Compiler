package backend.instructions;

import backend.operands.MipsOperand;

public class MipsStore extends MipsInstruction {
    // store的三个操作数均为use而非def
    public MipsStore(MipsOperand src, MipsOperand addr, MipsOperand offset) {
        super(src, addr, offset, true);
    }

    public MipsOperand getSrc() {
        return getSrc(1);
    }
    public MipsOperand getBase() {
        return getSrc(2);
    }
    public MipsOperand getOffset() {
        return getSrc(3);
    }
    public void setOffset(MipsOperand offset){
        setSrc(3, offset);
    }

    public String toString() {
        return "sw\t" + getSrc() + ",\t" + getOffset() + "(" + getBase() + ")\n";
    }
}
