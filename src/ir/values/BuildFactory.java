package ir.values;

import ir.types.ArrayType;
import ir.types.FunctionType;
import ir.types.IntegerType;
import ir.types.Type;
import ir.values.instructions.BinaryInst;
import ir.values.instructions.ConstArray;
import ir.values.instructions.ConvInst;
import ir.values.instructions.Operator;
import ir.values.instructions.mem.*;
import ir.values.instructions.terminator.BrInst;
import ir.values.instructions.terminator.CallInst;
import ir.values.instructions.terminator.RetInst;

import java.util.List;

public class BuildFactory {
    private static final BuildFactory instance = new BuildFactory();

    public BuildFactory(){
    }
    
    public static BuildFactory getInstance(){
        return instance;
    }

    // Functions
    public Function buildFunction(String name, Type returnType, List<Type> parametersTypes) {
        return new Function(name, getFunctionType(returnType, parametersTypes), false);
    }

    public Function buildLibraryFunction(String name, Type returnType, List<Type> parametersTypes) {
        return new Function(name, getFunctionType(returnType, parametersTypes), true);
    }

    public FunctionType getFunctionType(Type returnType, List<Type> parametersTypes) {
        return new FunctionType(returnType, parametersTypes);
    }

    public List<Value> getFunctionArguments(Function function) {
        return function.getArgumentList();
    }

    // BasicBlock
    public BasicBlock buildBasicBlock(Function function) {
        return new BasicBlock(function);
    }

    public void checkBlockEnd(BasicBlock basicBlock) {
        Type returnType = ((FunctionType) basicBlock.getNode().getParent().getValue().getType()).getReturnType();
        if (!basicBlock.getInstructions().isEmpty()) {
            Value lastInst = basicBlock.getInstructions().getEnd().getValue();
            if (lastInst instanceof RetInst || lastInst instanceof BrInst) {
                return;
            }
        }
        if (returnType instanceof IntegerType) {
            buildRet(basicBlock, ConstInt.zero);
        }
        else {
            buildRet(basicBlock);
        }
    }

    // BinaryInst
    public BinaryInst buildBinary(BasicBlock basicBlock, Operator op, Value left, Value right) {
        BinaryInst tmp = new BinaryInst(basicBlock, op, left, right);
        if (op == Operator.And || op == Operator.Or) {
            tmp = buildBinary(basicBlock, Operator.Ne, tmp, ConstInt.zero);
        }
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public BinaryInst buildNot(BasicBlock basicBlock, Value value) {
        return buildBinary(basicBlock, Operator.Eq, value, ConstInt.zero);
    }

    // Var
    public GlobalVar buildGlobalVar(String name, Type type, boolean isConst, Value value) {
        return new GlobalVar(name, type, isConst, value);
    }

    public AllocaInst buildVar(BasicBlock basicBlock, Value value, boolean isConst, Type allocaType) {
        AllocaInst tmp = new AllocaInst(basicBlock, isConst, allocaType);
        tmp.addInstToBlock(basicBlock);
        if (value != null) {
            if (allocaType == IntegerType.i8 && value.getType() == IntegerType.i32){
                Value truncValue = buildTrunc(value, basicBlock, IntegerType.i32, IntegerType.i8);
                buildStore(basicBlock, tmp, truncValue);
            }
            else if (allocaType == IntegerType.i32 && value.getType() == IntegerType.i8){
                Value zextValue = buildZext(value, basicBlock, IntegerType.i8, IntegerType.i32);
                buildStore(basicBlock, tmp, zextValue);
            }
            else buildStore(basicBlock, tmp, value);
        }
        return tmp;
    }

    public ConstInt getConstInt(int value, Type type) {
        return new ConstInt(value, type);
    }

    public ConstString getConstString(String value) {
        return new ConstString(value);
    }

    public ConstChar getConstChar(int value) {
        return new ConstChar(value);
    }

    // Array
    public GlobalVar buildGlobalArray(String name, Type type, boolean isConst) {
//        System.out.println(((ArrayType) type).getElementType());
        Value tmp = new ConstArray(type, ((ArrayType) type).getElementType(), ((ArrayType) type).getCapacity());
        return new GlobalVar(name, type, isConst, tmp);
    }

    public AllocaInst buildArray(BasicBlock basicBlock, boolean isConst, Type arrayType) {
        AllocaInst tmp = new AllocaInst(basicBlock, isConst, arrayType);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public void buildInitArray(Value array, int index, Value value) {
        ((ConstArray) ((GlobalVar) array).getValue()).storeValue(index, value);
    }

    public ArrayType getArrayType(Type elementType, int length) {
        return new ArrayType(elementType, length);
    }

    // ConvInst
    public Value buildZext(Value value, BasicBlock basicBlock, Type from, Type to) {
        if (value instanceof ConstInt) { // ??? constchar?
            return new ConstInt(((ConstInt) value).getValue());
        }
        ConvInst tmp = new ConvInst(basicBlock, Operator.Zext, value, from, to);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public Value buildTrunc(Value value, BasicBlock basicBlock, Type from, Type to) {
        if (value instanceof ConstInt) { // ??? constchar?
            return new ConstInt(((ConstInt) value).getValue(), IntegerType.i8); // ???
        }
        ConvInst tmp = new ConvInst(basicBlock, Operator.Trunc, value, from, to);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public ConvInst buildBitcast(Value value, BasicBlock basicBlock, Type from, Type to) {
        ConvInst tmp = new ConvInst(basicBlock, Operator.Bitcast, value, from, to);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public BinaryInst buildConvToI1(Value val, BasicBlock basicBlock) {
        BinaryInst tmp = new BinaryInst(basicBlock, Operator.Ne, val, getConstInt(0, IntegerType.i32));
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    // MemInst
    public LoadInst buildLoad(BasicBlock basicBlock, Value pointer) {
        LoadInst tmp = new LoadInst(basicBlock, pointer);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public StoreInst buildStore(BasicBlock basicBlock, Value pointer, Value value) {
        StoreInst tmp = new StoreInst(basicBlock, pointer, value);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public GEPInst buildGEP(BasicBlock basicBlock, Value pointer, List<Value> indices) {
        GEPInst tmp = new GEPInst(basicBlock, pointer, indices);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public GEPInst buildGEP(BasicBlock basicBlock, Value pointer, int offset) {
        GEPInst tmp = new GEPInst(basicBlock, pointer, offset);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public PhiInst buildPhi(BasicBlock basicBlock, Type type, List<Value> values) {
        PhiInst tmp = new PhiInst(basicBlock, type, values);
        tmp.addInstToBlockBegin(basicBlock);
        return tmp;
    }

    // TerminatorInst
    public BrInst buildBr(BasicBlock basicBlock, BasicBlock trueBlock) {
        BrInst tmp = new BrInst(basicBlock, trueBlock);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public BrInst buildBr(BasicBlock basicBlock, Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        BrInst tmp = new BrInst(basicBlock, cond, trueBlock, falseBlock);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public CallInst buildCall(BasicBlock basicBlock, Function function, List<Value> args) {
        CallInst tmp = new CallInst(basicBlock, function, args);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public RetInst buildRet(BasicBlock basicBlock) {
        RetInst tmp = new RetInst(basicBlock);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }

    public RetInst buildRet(BasicBlock basicBlock, Value ret) {
        RetInst tmp = new RetInst(basicBlock, ret);
        tmp.addInstToBlock(basicBlock);
        return tmp;
    }
}
