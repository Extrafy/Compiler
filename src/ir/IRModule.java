package ir;

import backend.MipsBuildingContext;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import backend.parts.MipsModule;
import ir.values.*;
import ir.values.instructions.Instruction;
import utils.MyList;
import utils.MyNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IRModule {
    private static final IRModule module = new IRModule();
    private List<GlobalVar> globalVars;

    private MyList<Function, IRModule> functions;

    private HashMap<Integer, Instruction> instructions;

    private IRModule(){
        this.globalVars = new ArrayList<>();
        this.functions = new MyList<>(this);
        this.instructions = new HashMap<>();
    }

    public static IRModule getInstance(){
        return module;
    }

    public void addGlobalVar(GlobalVar globalVar){
        this.globalVars.add(globalVar);
    }

    public List<GlobalVar> getGlobalVars() {
        return globalVars;
    }

    public void addInstruction(int handle, Instruction instruction){
        this.instructions.put(handle, instruction);
    }

    public MyList<Function, IRModule> getFunctions(){
        return this.functions;
    }

    public void refreshRegNum(){
        for (MyNode<Function, IRModule> function : functions){
            Value.REG_NUM = 0;
            function.getValue().refreshArgReg();
            if (!function.getValue().isLibFunc()){
                for (MyNode<BasicBlock, Function> basicBlock : function.getValue().getBlockList()){
                    if (basicBlock.getValue().getInstructions().isEmpty()){
                        BuildFactory.getInstance().checkBlockEnd(basicBlock.getValue());
                    }
                    basicBlock.getValue().setName(String.valueOf(Value.REG_NUM++));
                    basicBlock.getValue().refreshReg();
                }
            }
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (GlobalVar globalVar : globalVars) {
            s.append(globalVar.toString()).append("\n");
        }
        if (!globalVars.isEmpty()) {
            s.append("\n");
        }
        refreshRegNum();
        for (MyNode<Function, IRModule> function : functions) {
            if (function.getValue().isLibFunc()) {
                s.append("declare ").append(function.getValue().toString()).append("\n\n");
            }
            else {
                s.append("define dso_local ").append(function.getValue().toString()).append("{\n");
                for (MyNode<BasicBlock, Function> basicBlock : function.getValue().getBlockList()) {
                    if (basicBlock != function.getValue().getBlockList().getBegin()) {
                        s.append("\n");
                    }
                    s.append(";<label>:").append(basicBlock.getValue().getName()).append(":\n").append(basicBlock.getValue().toString());
                }
                s.append("}\n\n");
            }
        }
        return s.toString();
    }

    public void buildMips(){
        for(GlobalVar globalVariable : globalVars){
            globalVariable.buildMips();
        }
        mapFunctionBlockIrToMips();
        for (MyNode node : functions){
            ((Function)node.getValue()).buildMips();
        }
    }

    /**
     * 将中间代码的函数和基本块对象:
     * 1.构建mips里的相应对象
     * 2.加入Module
     * 3.信息存储到mips对象里
     */
    private void mapFunctionBlockIrToMips(){
        // 遍历所有函数
        for (MyNode node : functions){
            Function irFunction = (Function) node.getValue();
            // 构建函数对象
            MipsFunction mipsFunction = new MipsFunction(irFunction.getName(), irFunction.isLibFunc());

            MipsBuildingContext.addFunctionMapping(irFunction, mipsFunction);
            MipsModule.addFunction(mipsFunction);

            // 构建基本块对象
//            ArrayList<BasicBlock> blocks = irFunction.getBasicBlocks();
            for (MyNode node2 : irFunction.getBlockList()){
                BasicBlock irBlock = (BasicBlock) node2.getValue();
                MipsBlock mipsBlock = new MipsBlock(irBlock.getName(), irBlock.getLoopDepth());
                MipsBuildingContext.addBlockMapping(irBlock, mipsBlock);
            }
            // 记录mipsBlock的前驱块信息, 前驱块当然也是mipsBlock
            for (MyNode node2 : irFunction.getBlockList()){
                BasicBlock irBlock = (BasicBlock) node2.getValue();
                MipsBlock mipsBlock = MipsBuildingContext.b(irBlock);
                for(BasicBlock irPreBlock : irBlock.getPredecessors()){
                    mipsBlock.addPreBlock(MipsBuildingContext.b(irPreBlock));
                }
            }
        }
    }
}
