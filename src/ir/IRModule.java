package ir;

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
}
