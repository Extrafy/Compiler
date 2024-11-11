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
                    if ()
                }
            }
        }
    }
}
