package ir.values.instructions.mem;

import ir.types.Type;
import ir.values.BasicBlock;
import ir.values.instructions.Instruction;
import ir.values.instructions.Operator;

public class MemInst extends Instruction {

    public MemInst(Type type, Operator operator, BasicBlock block) {
        super(type, operator, block);
    }
}
