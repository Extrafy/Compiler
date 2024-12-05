package backend.operands;

public abstract class MipsOperand {

    public boolean isPrecolored() {
        return false;
    }

    public boolean needsColor() {
        return false;
    }

    public boolean isAllocated() {
        return false;
    }
}
