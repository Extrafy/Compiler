package ir.values;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.instructions.MipsInstruction;
import backend.instructions.MipsMove;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.operands.MipsVirtualReg;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import config.Config;
import ir.IRModule;
import ir.types.FunctionType;
import ir.types.Type;
import ir.values.instructions.Instruction;
import ir.values.instructions.mem.PhiInst;
import utils.MyList;
import utils.MyNode;
import utils.Pair;

import java.util.*;

public class Function extends Value{
    private final MyList<BasicBlock, Function> blockList;
    private final MyNode<Function, IRModule> node;
    private final List<Argument> argumentList;
    private final List<Function> predecessors;
    private final List<Function> successors;
    private final boolean isLibFunc;


    public Function(String name, Type type, boolean isLibFunc) {
        super(name, type);
        REG_NUM = 0;
        this.blockList = new MyList<>(this);
        this.node = new MyNode<>(this);
        this.argumentList = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.isLibFunc = isLibFunc;
        for (Type t : ((FunctionType) type).getParametersType()){
            argumentList.add(new Argument(t, ((FunctionType) type).getParametersType().indexOf(t), isLibFunc));
        }
        this.node.insertAtEnd(IRModule.getInstance().getFunctions());
    }

    public MyList<BasicBlock, Function> getBlockList() {
        return blockList;
    }

    public MyNode<Function, IRModule> getNode() {
        return node;
    }

    public List<Value> getArgumentList() {
        return new ArrayList<>(argumentList);
    }

    public List<Function> getPredecessors() {
        return predecessors;
    }

    public void addPredecessors(Function predecessor){
        this.predecessors.add(predecessor);
    }

    public List<Function> getSuccessors() {
        return successors;
    }

    public void addSuccessor(Function successor){
        this.successors.add(successor);
    }

    public boolean isLibFunc(){
        return isLibFunc;
    }

    public void refreshArgReg(){
        for (Argument argument : argumentList){
            argument.setName("%" + REG_NUM++);
        }
    }

    public BasicBlock getHeadBlock() {
         // ???mips
        return blockList.getBegin().getValue();
    }

    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append(((FunctionType) this.getType()).getReturnType()).append(" @").append(this.getName()).append("(");
        for (int i = 0; i < argumentList.size(); i++) {
            s.append(argumentList.get(i).getType());
            if (i != argumentList.size() - 1) {
                s.append(", ");
            }
        }
        s.append(")");
        return s.toString();
    }

//    private final HashMap<Pair<MipsBlock, MipsBlock>, ArrayList<MipsInstruction>> phiCopysLists = new HashMap<>();

    public void buildMips() {
        // 非内建函数才需要解析
        if (!isLibFunc) {
            MipsBuildingContext.curIrFunction = this;
            // 解析块
            for (MyNode node : blockList) {
                ((BasicBlock)node.getValue()).buildMips();
            }
            // 将块加入函数，并完善跳转关系
            MipsFunction mipsFunction = MipsBuildingContext.f(this);
            MipsBlock firstMipsBlock = MipsBuildingContext.b(getHeadBlock());
            // 开启Mem2Reg以及PHI优化
            if (Config.openMem2RegOpt) {
//                parsePhis();
//                System.out.println("[字典]\n" + phiCopysLists + "\n[进入Serial]\n");
//                mipsFunction.blockSerialPHI(firstMipsBlock, phiCopysLists);
//                mipsFunction.blockSerialize(firstMipsBlock);
            }
            // 关闭Mem2Reg以及PHI优化
            else {
                mipsFunction.blockSerialize(firstMipsBlock);
            }
        }
    }

    // PHI优化
//    private void parsePhis() {
//        // 遍历函数中的每个块
//        for (MyNode node : blockList) {
//            BasicBlock block = ((BasicBlock)node.getValue());
//            MipsBlock mipsBlock = MipsBuildingContext.b(block);
//
//            List<BasicBlock> predBlocks = block.getPredecessors();
////            System.out.println("基本块:" + block + "[preBlocks]" + predBlocks);
//            int predNum = predBlocks.size();
//            if (predNum <= 1) {
//                continue;
//            }
//            // 收集基本块中的 phi 指令
//            ArrayList<PhiInst> phis = new ArrayList<>();
//            for (MyNode node2 : block.getInstructions()) {
//                Instruction instruction = (Instruction) node2.getValue();
//                if (instruction instanceof PhiInst) {
//                    phis.add((PhiInst) instruction);
//                }
//                else {
//                    break;
//                }
//            }
//
//
//            for (BasicBlock preBlock : predBlocks) {
//                // 前驱-后继
//                Pair<MipsBlock, MipsBlock> pair = new Pair<>(MipsBuildingContext.b(preBlock), mipsBlock);
////                Pair<MipsBlock, MipsBlock> pair2 = new Pair<>(MipsBuildingContext.b(preBlock), mipsBlock);
//                phiCopysLists.put(pair, genPhiCopys(phis, preBlock, block));
////                System.out.println("[放入字典]\n" + MipsBuildingContext.b(preBlock).getName() + " " + mipsBlock.getName());
////                System.out.println("[放入后当场查找]\n" + phiCopysLists.getOrDefault(pair2, new ArrayList<>()));
//            }
////            System.out.println("==========================================");
////            System.out.println("基本块:" + block + "\n[phiCopysLists]\n" + phiCopysLists);
////            System.out.println("==========================================");
//        }
//
//    }
//
//    private ArrayList<MipsInstruction> genPhiCopys(ArrayList<PhiInst> phis, BasicBlock irPreBlock, BasicBlock block) {
//        MipsFunction mipsFunction = MipsBuildingContext.f(this);
//        // 通过构建一个图来检验是否成环
//        HashMap<MipsOperand, MipsOperand> graph = new HashMap<>();
//
//        ArrayList<MipsInstruction> copys = new ArrayList<>();
//
//        // 构建一个图
//        for (PhiInst phi : phis) {
//            MipsOperand phiTarget = MipsBuilder.buildOperand(phi, false, this, block);
//            // 该preBlock对应的phi里的Value
//            Value inputValue = phi.getInputValForBlock(irPreBlock);
//            // 这里进行了一个复杂的讨论，这是因为一般的 parseOperand 在分析立即数的时候，可能会引入
//            // 其他指令，而这些指令会跟在当前块上，而不是随意移动的（我们需要他们随意移动）
//            MipsOperand phiSrc;
//            if (inputValue instanceof ConstInt) {
//                phiSrc = new MipsImm(((ConstInt) inputValue).getValue());
//            } else {
//                phiSrc = MipsBuilder.buildOperand(inputValue, true, this, block);
//            }
//            graph.put(phiTarget, phiSrc);
//        }
//
//        while (!graph.isEmpty()) {
//            Stack<MipsOperand> path = new Stack<>();
//            MipsOperand cur;
//            // 对这个图进行 DFS 遍历来获得成环信息, DFS 发生了不止一次，而是每次检测到一个环就会处理一次
//            for (cur = graph.entrySet().iterator().next().getKey(); graph.containsKey(cur); cur = graph.get(cur)) {
//                // 这就说明成环了，也就是会有 swap 问题
//                if (path.contains(cur)) {
//                    break;
//                } else {
//                    path.push(cur);
//                }
//            }
//            if (!graph.containsKey(cur)) {
//                handleNoCyclePath(path, cur, copys, graph);
//            } else {
//                handleCyclePath(mipsFunction, path, cur, copys, graph);
//                handleNoCyclePath(path, cur, copys, graph);
//            }
//        }
//        return copys;
//    }
//
//    private void handleNoCyclePath(Stack<MipsOperand> path, MipsOperand begin, ArrayList<MipsInstruction> copys, HashMap<MipsOperand, MipsOperand> graph) {
//        MipsOperand phiSrc = begin;
//        while (!path.isEmpty()) {
//            MipsOperand phiTarget = path.pop();
//            MipsInstruction move = new MipsMove(phiTarget, phiSrc);
//            copys.add(0, move);
//            phiSrc = phiTarget;
//            graph.remove(phiTarget);
//        }
//    }
//
//    private void handleCyclePath(MipsFunction MipsFunction, Stack<MipsOperand> path, MipsOperand begin, ArrayList<MipsInstruction> copys, HashMap<MipsOperand, MipsOperand> graph) {
//        MipsVirtualReg tmp = new MipsVirtualReg();
//        MipsFunction.addUsedVirtualReg(tmp);
//
//        MipsMove move = new MipsMove(tmp, null);
//        while (path.contains(begin)) {
//            MipsOperand r = path.pop();
//            move.setSrc(r);
//            copys.add(move);
//            move = new MipsMove(r, null);
//            graph.remove(r);
//        }
//        move.setSrc(tmp);
//    }



    public static class Argument extends Value{
        private int idx;

        public Argument(String name, Type type, int idx) {
            super(name, type);
            this.idx = idx;
        }

        public Argument(Type type, int idx, boolean isLibFunc){
            super(isLibFunc ? "" : "%" + REG_NUM++, type);
            this.idx = REG_NUM-1;
        }

        public int getIdx(){
            return idx;
        }

        public String toString(){
            return this.getType().toString() + " " + this.getName();
        }
    }
}
