package backend.instructions;

import backend.parts.MipsFunction;

public class MipsCall extends MipsInstruction{

    // 要调用的函数
    private MipsFunction function;

    public MipsCall(MipsFunction function) {
        super();
        this.function = function;
    }

    public String toString() {
        return "jal\t" + function.getName() + "\n";
    }
}
