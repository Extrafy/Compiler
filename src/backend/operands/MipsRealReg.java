package backend.operands;

import java.util.Objects;

public class MipsRealReg extends MipsOperand{
    private RegType type;
    private boolean isAllocated;

    public final static MipsRealReg ZERO = new MipsRealReg(0);
    public final static MipsRealReg AT = new MipsRealReg("at");
    public final static MipsRealReg SP = new MipsRealReg("sp");
    public final static MipsRealReg RA = new MipsRealReg("ra");
    public final static MipsRealReg V0 = new MipsRealReg("v0");

    public MipsRealReg(int index) {
        this.type = RegType.getRegType(index);
        this.isAllocated = false;
    }
    public MipsRealReg(String name) {
        this.type = RegType.getRegType(name);
        this.isAllocated = false;
    }
    public MipsRealReg(int index, boolean isAllocated) {
        this.type = RegType.getRegType(index);
        this.isAllocated = isAllocated;
    }
    public MipsRealReg(RegType type, boolean isAllocated) {
        this.type = type;
        this.isAllocated = isAllocated;
    }

    // 获取物理寄存器编号
    public int getIndex(){
        return type.getIndex();
    }
    public RegType getType(){
        return type;
    }

    public boolean isPrecolored() {
        return !isAllocated;
    }

    public boolean isAllocated() {
        return isAllocated;
    }

    public void setAllocated(boolean allocated) {
        isAllocated = allocated;
    }

    public boolean needsColor() {
        return !isAllocated;
    }

    public String toString() {
        return "$" + type.getName();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsRealReg reg = (MipsRealReg) o;
        return type == reg.type && isAllocated == reg.isAllocated;
    }

    public int hashCode() {
        return Objects.hash(type.index, isAllocated);
    }
}
