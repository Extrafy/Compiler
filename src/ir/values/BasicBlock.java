package ir.values;

import ir.types.FunctionType;
import ir.types.LabelType;
import ir.types.VoidType;
import ir.values.instructions.Instruction;
import ir.values.instructions.mem.StoreInst;
import ir.values.instructions.terminator.BrInst;
import ir.values.instructions.terminator.CallInst;
import ir.values.instructions.terminator.RetInst;
import utils.MyList;
import utils.MyNode;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value{
    private MyList<Instruction, BasicBlock> instructions;
    private MyNode<BasicBlock, Function> node;
    private List<BasicBlock> predecessors; // 前驱 BasicBlocks
    private List<BasicBlock> successors; // 后继 BasicBlocks

    public BasicBlock(Function function) {
        super(String.valueOf(REG_NUM++), new LabelType());
        this.instructions = new MyList<>(this);
        this.node = new MyNode<>(this);
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.node.insertAtEnd(function.getBlockList());
    }

    public MyList<Instruction, BasicBlock> getInstructions() {
        return instructions;
    }

    public void setInstructions(MyList<Instruction, BasicBlock> instructions) {
        this.instructions = instructions;
    }

    public MyNode<BasicBlock, Function> getNode() {
        return node;
    }

    public void setNode(MyNode<BasicBlock, Function> node) {
        this.node = node;
    }

    public List<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(List<BasicBlock> predecessors) {
        this.predecessors = predecessors;
    }

    public void addPredecessor(BasicBlock predecessor) {
        this.predecessors.add(predecessor);
    }

    public List<BasicBlock> getSuccessors() {
        return successors;
    }

    public void setSuccessors(List<BasicBlock> successors) {
        this.successors = successors;
    }

    public void addSuccessor(BasicBlock successor) {
        this.successors.add(successor);
    }

    public Function getParent() {
        return this.node.getParent().getValue();
    }

    public void refreshReg() {
        for (MyNode<Instruction, BasicBlock> node : this.instructions) {
            Instruction instruction = node.getValue();
            if (!(instruction instanceof StoreInst || instruction instanceof BrInst || instruction instanceof RetInst ||
                    (instruction instanceof CallInst && ((FunctionType) instruction.getOperands().get(0).getType()).getReturnType() instanceof VoidType))) {
                instruction.setName("%" + REG_NUM++);
            }
        }
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (MyNode<Instruction, BasicBlock> instruction : this.instructions) {
            s.append("\t").append(instruction.getValue().toString()).append("\n");
        }
        return s.toString();
    }
}
