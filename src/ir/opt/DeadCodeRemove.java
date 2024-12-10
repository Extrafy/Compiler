package ir.opt;

import AST.AST;
import ir.IRModule;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.instructions.Instruction;
import ir.values.instructions.terminator.BrInst;
import ir.values.instructions.terminator.RetInst;
import utils.MyList;
import utils.MyNode;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author : Extrafy
 * description  :
 * createDate   : 2024/12/10 18:50
 */
// 简单的死代码删除。删除基本块中，ret和Br后面的死代码
public class DeadCodeRemove {

    public static void analyze() {
        MyList<Function, IRModule> nodes = IRModule.getInstance().getFunctions();
        ArrayList<Function> functions = new ArrayList<>();
        for (MyNode<Function, IRModule> node : nodes){
            Function irFunction = node.getValue();
            functions.add(irFunction);
        }
        for (Function function : functions) {
            if (!function.isLibFunc()) {
                for (MyNode<BasicBlock,Function> node : function.getBlockList()){
                    delDeadCode(node.getValue());
                }
            }
        }
    }
    public static void delDeadCode(BasicBlock block){
        LinkedList<Instruction> instructions = new LinkedList<>();
        for (MyNode<Instruction, BasicBlock> node : block.getInstructions()){
            instructions.add(node.getValue());
        }
        boolean flag = false;
        for(Instruction instruction : instructions){
            if(flag){
                instruction.dropAllOperands();
                instruction.eraseFromParent();
            }
            else if(instruction instanceof RetInst || instruction instanceof BrInst){
                flag = true;
            }
        }
    }
}

