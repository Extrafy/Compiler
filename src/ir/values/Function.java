package ir.values;

import ir.IRModule;
import ir.types.FunctionType;
import ir.types.Type;
import utils.MyList;
import utils.MyNode;

import java.util.ArrayList;
import java.util.List;

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

    public static class Argument extends Value{
        private int idx;

        public Argument(String name, Type type, int idx) {
            super(name, type);
            this.idx = idx;
        }

        public Argument(Type type, int idx, boolean isLibFunc){
            super(isLibFunc ? "" : "%" + REG_NUM++, type);
            this.idx = idx;
        }

        public int getIdx(){
            return idx;
        }

        public String toString(){
            return this.getType().toString() + " " + this.getName();
        }
    }
}
