package backend.operands;

import java.util.Objects;

public class MipsVirtualReg extends MipsOperand{
    private static int nameNum = 0;
    private String name;

    private int getNameNum(){
        return nameNum++;
    }
    public MipsVirtualReg() {
        this.name = "v" + getNameNum();
    }

    // 虚拟寄存器都需要着色
    public boolean needsColor() {
        return true;
    }

    public String toString() {
        return name;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsVirtualReg that = (MipsVirtualReg) o;
        return Objects.equals(name, that.name);
    }

    public int hashCode() {
        return Objects.hash(name);
    }
}
