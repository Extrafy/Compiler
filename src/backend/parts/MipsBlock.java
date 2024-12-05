package backend.parts;

import backend.instructions.MipsInstruction;

import java.util.ArrayList;
import java.util.LinkedList;

public class MipsBlock {
    private static int nameNum = 0;
    private String name;
    // 指令列表
    private LinkedList<MipsInstruction> instructions = new LinkedList<>();

    private int loopDepth = 0;
    // 如果最后一条指令是有条件跳转指令，直接后继块。false指条件跳转中不满足条件下继续执行的基本块
    private MipsBlock falseSucBlock = null;
    // 一个基本块最多两个后继块，如果基本块只有一个后继，那么falseSucBlock是null，trueSucBlock不是null
    private MipsBlock trueSucBlock = null;
    // 前驱块
    private final ArrayList<MipsBlock> preBlocks = new ArrayList<>();

    public MipsBlock(String name, int loopDepth) {
        // 需要去除开头的;<label>:
        this.name = "b" + name + "_" + getNameNum();
        this.loopDepth = loopDepth;
    }
    // 由phi生长出来的Block
    public MipsBlock(int loopDepth) {
        this.name = "t_" + getNameNum();
        this.loopDepth = loopDepth;
    }
    public static int getNameNum(){
        return nameNum++;
    }
    public String getName() {
        return name;
    }
    public int getLoopDepth() {
        return loopDepth;
    }

    // ===== 前驱与后继块 =======
    public void addPreBlock(MipsBlock preBlock) {
        this.preBlocks.add(preBlock);
    }
    public void removePreBlock(MipsBlock preBlock) {
        this.preBlocks.remove(preBlock);
    }
    public ArrayList<MipsBlock> getPreBlocks() {
        return preBlocks;
    }
    public void setFalseSucBlock(MipsBlock falseSucBlock) {
        this.falseSucBlock = falseSucBlock;
    }
    public void setTrueSucBlock(MipsBlock trueSucBlock) {
        this.trueSucBlock = trueSucBlock;
    }
    public MipsBlock getFalseSucBlock() {
        return falseSucBlock;
    }
    public MipsBlock getTrueSucBlock() {
        return trueSucBlock;
    }

    // ======= 指令序列 ========
    public LinkedList<MipsInstruction> getInstructions() {
        return instructions;
    }
    public void setInstructions(LinkedList<MipsInstruction> instructions) {
        this.instructions = instructions;
    }
    public void addInstruction(MipsInstruction instruction){
        instructions.add(instruction);
    }
    public void addInstructionHead(MipsInstruction instruction) {
        instructions.addFirst(instruction);
    }
    public void removeInstruction() {
        instructions.removeLast();
    }
    public MipsInstruction getTailInstruction() {
        return instructions.getLast();
    }

    /**
     *  phi 指令解析的时候会产生一大堆没有归属的 mov 指令
     *  如果这个块只有一个后继块，那么我们需要把这些 mov 指令插入到最后一条跳转指令之前，这样就可以完成 phi 的更新
     */
    public void insertPhiMovesTail(ArrayList<MipsInstruction> phiMoves) {
        for (MipsInstruction phiMove : phiMoves) {
            instructions.add(instructions.size()-1, phiMove);
        }
    }

    /**
     * phiMoves 的顺序已经是正确的了，所以这个方法会确保 phiMoves 按照其原来的顺序插入到 block 的头部
     * @param phiMoves 待插入的 copy 序列
     */
    public void insertPhiCopysHead(ArrayList<MipsInstruction> phiMoves) {
        for (int i = phiMoves.size() - 1; i >= 0; i--) {
            instructions.addFirst(phiMoves.get(i));
        }
    }
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        // 块标签
        s.append(name).append(":\n");

        for(MipsInstruction instruction : instructions){
            s.append("\t").append(instruction);
        }

        return s.toString();
    }
}
