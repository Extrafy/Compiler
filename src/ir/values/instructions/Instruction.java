package ir.values.instructions;

import ir.types.Type;
import ir.values.*;
import ir.values.instructions.mem.*;
import ir.values.instructions.terminator.BrInst;
import ir.values.instructions.terminator.CallInst;
import ir.values.instructions.terminator.RetInst;
import ir.values.instructions.terminator.TerminatorInst;
import utils.MyNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public abstract class Instruction extends User {
    private Operator operator;
    private MyNode<Instruction, BasicBlock> node;
    private int handler;
    private static int HANDLER = 0;


    public Instruction(Type type, Operator operator, BasicBlock block) {
        super("", type);
        this.operator = operator;
        this.node = new MyNode<>(this);
        this.handler = HANDLER ++ ;
        this.getModule().addInstruction(handler, this);
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public MyNode<Instruction, BasicBlock> getNode() {
        return node;
    }

    public void setNode(MyNode<Instruction, BasicBlock> node) {
        this.node = node;
    }

    public int getHandler() {
        return handler;
    }

    public void setHandler(int handler) {
        this.handler = handler;
    }

    public static int getHANDLER() {
        return HANDLER;
    }

    public static void setHANDLER(int HANDLER) {
        Instruction.HANDLER = HANDLER;
    }

    public BasicBlock getParent(){
        return this.getNode().getParent().getValue();
    }

    public void addInstToBlock(BasicBlock basicBlock) { // ???
        if (basicBlock.getInstructions().getEnd() == null || (!(basicBlock.getInstructions().getEnd().getValue() instanceof BrInst) && !(basicBlock.getInstructions().getEnd().getValue() instanceof RetInst))) {
            this.getNode().insertAtEnd(basicBlock.getInstructions());
        }
        else {
            this.removeUserFromOperands();
        }
    }

    public void addInstToBlockBegin(BasicBlock basicBlock) {
        this.getNode().insertAtBegin(basicBlock.getInstructions());
    }

    public Instruction copySelf(Map<Value, Value> replaceMap) {  // ???
        Instruction copyInst = null;
        BuildFactory factory = BuildFactory.getInstance();
        BasicBlock fatherBB2insert = (BasicBlock) replaceMap.get(this.getParent());
        if (this instanceof MemInst) {
            switch (this.getOperator()) {
                case Alloca:
                    copyInst = new AllocaInst(fatherBB2insert, ((AllocaInst) this).isConst(), ((AllocaInst) this).getAllocaType());
                    copyInst.getNode().insertAtEnd(fatherBB2insert.getInstructions());
                    break;
                case Load:
                    copyInst = factory.buildLoad(fatherBB2insert, findValue(replaceMap, ((LoadInst) this).getPointer()));
                    break;
                case Store:
                    copyInst = factory.buildStore(fatherBB2insert,
                            findValue(replaceMap, ((StoreInst) this).getPointer()),
                            findValue(replaceMap, ((StoreInst) this).getValue()));
                    break;
                case GEP:
                    ArrayList<Value> values = new ArrayList<>();
                    for (int i = 1; i < this.getOperands().size(); i++) {
                        values.add(findValue(replaceMap, getOperands().get(i)));
                    }
                    copyInst = factory.buildGEP(fatherBB2insert, findValue(replaceMap, ((GEPInst) this).getPointer()), values);
                    break;
                case Phi:
                    copyInst = factory.buildPhi(
                            fatherBB2insert,
                            this.getType(),
                            new ArrayList<>(Collections.nCopies(this.getOperands().size(), getType() instanceof ConstInt ? ConstInt.zero : null))
                    );
                    break;
                default:
                    throw new RuntimeException();
            }
        } else if (this instanceof TerminatorInst) {
            switch (this.getOperator()) {
                case Br:
                    if (this.getOperands().size() == 1) {
                        copyInst = factory.buildBr(fatherBB2insert, (BasicBlock) findValue(replaceMap, ((BrInst) this).getTarget()));
                    } else if (this.getOperands().size() == 3) {
                        copyInst = factory.buildBr(fatherBB2insert,
                                findValue(replaceMap, getOperands().get(0)),
                                (BasicBlock) findValue(replaceMap, getOperands().get(1)),
                                (BasicBlock) findValue(replaceMap, getOperands().get(2)));
                    }
                    break;
                case Call:
                    ArrayList<Value> args = new ArrayList<>();
                    for (int i = 1; i < getOperands().size(); i++) {
                        args.add(findValue(replaceMap, getOperands().get(i)));
                    }
                    copyInst = factory.buildCall(fatherBB2insert, ((CallInst) this).getCalledFunction(), args);
                    ((CallInst) this).getCalledFunction().getPredecessors().remove(fatherBB2insert.getParent());
                    fatherBB2insert.getParent().getSuccessors().remove(((CallInst) this).getCalledFunction());
                    break;
                case Ret:
                    if (this.getOperands().size() == 1) {
                        copyInst = factory.buildRet(fatherBB2insert, findValue(replaceMap, getOperands().get(0)));
                    } else {
                        copyInst = factory.buildRet(fatherBB2insert);
                    }
                    break;
            }
        } else if (this instanceof BinaryInst) {
            copyInst = factory.buildBinary(fatherBB2insert, this.getOperator(),
                    findValue(replaceMap, getOperands().get(0)),
                    findValue(replaceMap, getOperands().get(1)));
        } else if (this instanceof ConvInst) {
            switch (this.getOperator()) {
                case Zext:
                    copyInst = (Instruction) factory.buildZext(findValue(replaceMap, getOperands().get(0)), fatherBB2insert);
                    break;
                case Bitcast:
                    copyInst = factory.buildBitcast(findValue(replaceMap, getOperands().get(0)), fatherBB2insert);
                    break;
            }
        }
        return copyInst;
    }

    public Value findValue(Map<Value, Value> replaceMap, Value value) {   // ???
        if (value instanceof ConstInt) {
            return value;
        } else {
            return replaceMap.get(value);
        }
    }
}
