package ir.values;

import ir.opt.Loop;
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
import java.util.HashSet;
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

    // =========== IR analyze ===============
    /**
     * 前驱与后继块
     */
    private final HashSet<BasicBlock> preBlocks = new HashSet<>();
    private final HashSet<BasicBlock> sucBlocks = new HashSet<>();
    public void addPreBlock(BasicBlock preBlock) {
        preBlocks.add(preBlock);
    }
    public void addSucBlock(BasicBlock sucBlock) {
        sucBlocks.add(sucBlock);
    }
    public HashSet<BasicBlock> getSucBlocks() {
        return sucBlocks;
    }
    public HashSet<BasicBlock> getPreBlocks() {
        return preBlocks;
    }

    // =========================================
    /**
     * 支配者块
     */
    private final ArrayList<BasicBlock> domers = new ArrayList<>();
    /**
     * 直接支配的基本块
     */
    private final ArrayList<BasicBlock> idomees = new ArrayList<>();
    /**
     * 直接支配基本块
     */
    private BasicBlock Idomer;
    /**
     * 在支配树中的深度
     */
    private int domLevel;

    /**
     * 支配边际，即刚好不被当前基本块支配的基本块
     */
    private final HashSet<BasicBlock> dominanceFrontier = new HashSet<>();
    public ArrayList<BasicBlock> getDomers() {
        return domers;
    }
    /**
     * 当前块是否是另一个块的支配者
     */
    public boolean isDominating(BasicBlock other) {
        return other.domers.contains(this);
    }

    public ArrayList<BasicBlock> getIdomees()
    {
        return idomees;
    }

    public void setIdomer(BasicBlock idomer)
    {
        Idomer = idomer;
    }

    public void setDomLevel(int domLevel) {
        this.domLevel = domLevel;
    }

    public int getDomLevel()
    {
        return domLevel;
    }

    public HashSet<BasicBlock> getDominanceFrontier() {
        return dominanceFrontier;
    }
    public BasicBlock getIdomer()
    {
        return Idomer;
    }

    // =========================================
    /**
     * 当前块直属的循环
     */
    private Loop loop = null;
    /**
     * 获得循环深度
     * 如果不在循环中，则深度为 1
     * @return 循环深度
     */
    public int getLoopDepth() {
        if (loop == null) {
            return 0;
        }
        return loop.getLoopDepth();
    }

    public void setLoop(Loop loop) {
        this.loop = loop;
    }

    public Loop getLoop() {
        return loop;
    }


    // ============ Mips generate ==============
    public void buildMips(){
        for(MyNode node : instructions){
            ((Instruction)node.getValue()).buildMips();
        }
    }
}
