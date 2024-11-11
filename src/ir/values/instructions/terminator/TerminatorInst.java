package ir.values.instructions.terminator;

import ir.types.Type;
import ir.values.BasicBlock;
import ir.values.instructions.Instruction;
import ir.values.instructions.Operator;

public class TerminatorInst extends Instruction {

    public TerminatorInst(Type type, Operator operator, BasicBlock block) {
        super(type, operator, block);
    }
}
