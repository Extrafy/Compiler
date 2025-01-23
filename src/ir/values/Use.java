package ir.values;

public class Use {
    private User user;
    private Value value;
    private int operandPos; // 在 OperandList 中的位置

    public Use(User user, Value value, int operandPos) {
        this.user = user;
        this.value = value;
        this.operandPos = operandPos;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public int getOperandPos() {
        return operandPos;
    }

    public void setOperandPos(int operandPos) {
        this.operandPos = operandPos;
    }
}
