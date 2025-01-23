package backend.operands;

public class MipsImm extends MipsOperand{
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public MipsImm(int value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(value);
    }
}
