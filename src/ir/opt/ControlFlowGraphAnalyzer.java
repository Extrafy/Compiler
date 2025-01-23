package ir.opt;

import ir.IRModule;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.instructions.Instruction;
import ir.values.instructions.terminator.BrInst;
import utils.MyList;
import utils.MyNode;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author : Extrafy
 * description  :
 * createDate   : 2024/12/10 18:50
 */
public class ControlFlowGraphAnalyzer {
    /**
     * 入口方法 开始进行分析
     */
    public static void analyze() {
        for (MyNode<Function, IRModule> node : IRModule.getInstance().getFunctions()) {
            Function function = node.getValue();
            if (!function.isLibFunc()) {
                buildBlockPreAndSuc(function);
            }
        }
    }

    /**
     * 构建指定函数的控制流
     */
    public static void buildBlockPreAndSuc(Function function) {
        // 重置函数内 所有块的前驱和后继关系
//        for(BasicBlock block : function.getBasicBlocks()){
//            block.getSucBlocks().clear();
//            block.getPreBlocks().clear();
//        }
        // 从首个块开始dfs，因为该dfs一定可以到达所有的控制块（仅考虑连通性）
        BasicBlock entryBlock = function.getHeadBlock();
        dfsBlock(entryBlock);
        // 删除无用的基本块
        delUnreachableBlock(function, entryBlock);
    }

    private static final HashSet<BasicBlock> visited = new HashSet<>();

    /**
     * DFS
     * 构建指定块的控制流
     */
    private static void dfsBlock(BasicBlock curBlock) {
        visited.add(curBlock);
        Instruction instruction = curBlock.getInstructions().getLast().getValue();
        BasicBlock sucBlock;
        if (instruction instanceof BrInst br) {
            if (br.isCondBr()) {
                // true
                sucBlock = (BasicBlock) br.getOperand(1);
                addEdgeAndVisit(curBlock, sucBlock);
                // false
                sucBlock = (BasicBlock) br.getOperand(2);
                addEdgeAndVisit(curBlock, sucBlock);
            }
            else {
                sucBlock = (BasicBlock) br.getOperand(0);
                addEdgeAndVisit(curBlock, sucBlock);
            }
        }
    }

    /**
     * 确立前驱和后继的关系，并尝试访问后继
     */
    private static void addEdgeAndVisit(BasicBlock preBlock, BasicBlock sucBlock) {
        preBlock.addSucBlock(sucBlock);
        sucBlock.addPreBlock(preBlock);
        if (!visited.contains(sucBlock)) {
            dfsBlock(sucBlock);
        }
    }

    /**
     * 清除指定函数内不可到达的基本块
     */
    private static void delUnreachableBlock(Function function, BasicBlock entryBlock) {
        MyList<BasicBlock, Function> newBlocks = new MyList<>(function);
//        ArrayList<BasicBlock> newBlocks = new ArrayList<>();

        // 遍历所有基本块
        for (MyNode<BasicBlock, Function> node : function.getBlockList()) {
            BasicBlock block = node.getValue();
            // 找到了没有前驱且不是入口的基本块
            if (block.getPreBlocks().isEmpty() && block != entryBlock) {
                // 删除其后继结点与自己的关系
                for (BasicBlock sucBlock : block.getSucBlocks()) {
                    sucBlock.getPreBlocks().remove(block);
                }
                // 清除指令内的所有user引用
                for (MyNode<Instruction, BasicBlock> node1 : block.getInstructions()) {
                    Instruction instruction = node1.getValue();
                    instruction.dropAllOperands();
                }
                block.delAllInstruction();
            }
            // 是不需要删除的块
            else {
                node.insertAtEnd(newBlocks);
            }
        }
        // 为函数设置新的基本块列表
        function.setBlockList(newBlocks);
//        function.setBasicBlocks(newBlocks);
    }
}

