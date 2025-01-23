package backend.instructions;

import backend.operands.RegType;
import backend.parts.MipsFunction;

public class MipsRet extends MipsInstruction {
    // 所属的函数
    private MipsFunction function;

    public MipsRet(MipsFunction function) {
        this.function = function;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        int stackSize = function.getTotalStackSize();
        // 返回前将SP复位
        if (stackSize != 0) {
            s.append("add\t$sp, \t$sp,\t").append(stackSize).append("\n");
        }
        // 主函数直接结束运行
        if (function.getName().equals("main")) {
            s.append("\tli\t$v0,\t10\n");
            s.append("\tsyscall\n\n");
        }
        // 非主函数，需要恢复现场
        else {
            // 在返回之前回复寄存器寄存器
            int stackOffset = -4;
            for (RegType regType : function.getRegsNeedSaving()) {
                s.append("\t").append("lw\t").append(regType).append(",\t").append(stackOffset).append("($sp)\n");
                stackOffset -= 4;
            }
            // 跳转返回
            s.append("\tjr\t$ra\n");
        }

        return s.toString();
    }
}

