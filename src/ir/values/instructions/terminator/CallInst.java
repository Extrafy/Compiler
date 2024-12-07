package ir.values.instructions.terminator;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.instructions.*;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import ir.types.FunctionType;
import ir.types.IntegerType;
import ir.types.Type;
import ir.types.VoidType;
import ir.values.*;
import ir.values.instructions.ConvInst;
import ir.values.instructions.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CallInst extends TerminatorInst{
    public CallInst(BasicBlock block, Function function, List<Value> args) {
        super(((FunctionType) function.getType()).getReturnType(), Operator.Call, block);

        if (!(((FunctionType) function.getType()).getReturnType() instanceof VoidType)) {
            this.setName("%" + REG_NUM++);
        }
        this.addOperand(function);

        for (int i = 0; i < args.size(); i++) {
            Type curType = args.get(i).getType();
            Type realType = ((FunctionType) function.getType()).getParametersType().get(i);
            Value t = convertType(args.get(i), block, curType, realType);
            this.addOperand(t);
        }

        Function curFunction = block.getNode().getParent().getValue();
        function.addPredecessors(curFunction);
        curFunction.addSuccessor(function);
    }

    private Value convertType(Value value, BasicBlock basicBlock, Type curType, Type realType) {
        // 可能需要修改
        boolean isCurI1 = curType instanceof IntegerType && ((IntegerType) curType).isI1();
        boolean isCurI8 = curType instanceof IntegerType && ((IntegerType) curType).isI8();
        boolean isCurI32 = curType instanceof IntegerType && ((IntegerType) curType).isI32();
        boolean isRealI1 = realType instanceof IntegerType && ((IntegerType) realType).isI1();
        boolean isRealI8 = realType instanceof IntegerType && ((IntegerType) realType).isI8();
        boolean isRealI32 = realType instanceof IntegerType && ((IntegerType) realType).isI32();
        if (!isCurI1 && !isCurI32 && !isRealI1 && !isRealI32) {
            return value;
        }
        else if ((isCurI1 && isRealI1) || (isCurI32 && isRealI32)) {
            return value;
        }
        else if (isCurI1 && isRealI32) {
            return BuildFactory.getInstance().buildZext(value, basicBlock, IntegerType.i1, IntegerType.i32);
        }
        else if (isCurI8 && isRealI32) {
            return BuildFactory.getInstance().buildZext(value, basicBlock, IntegerType.i8, IntegerType.i32);
        }
        else if (isCurI32 && isRealI1) {
            return BuildFactory.getInstance().buildConvToI1(value, basicBlock);
        }
        else if (isCurI32 && isRealI8) {
            return BuildFactory.getInstance().buildTrunc(value, basicBlock, IntegerType.i32, IntegerType.i8);
        }
        else {
            return value;
        }
    }

    public Function getCalledFunction() {
        return (Function) this.getOperands().get(0);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        Type returnType = ((FunctionType) this.getCalledFunction().getType()).getReturnType();
        if (returnType instanceof VoidType) {
            s.append("call ");
        }
        else {
            s.append(this.getName()).append(" = call ");
        }
        s.append(returnType.toString()).append(" @").append(this.getCalledFunction().getName()).append("(");
        for (int i = 1; i < this.getOperands().size(); i++) {
            s.append(this.getOperands().get(i).getType().toString()).append(" ").append(this.getOperands().get(i).getName());
            if (i != this.getOperands().size() - 1) {
                s.append(", ");
            }
        }
        s.append(")");
        return s.toString();
    }

    public ArrayList<Value> getArgs() {
        ArrayList<Value> args = new ArrayList<>();
        for (int i = 0; i < ((Function) this.getOperands().get(0)).getArgumentList().size(); i++) {
            args.add(this.getOperands().get(i+1));
        }
        return args;
    }

    public void buildMips() {
        MipsBlock mipsBlock = MipsBuildingContext.b(getParent());
        MipsFunction mipsFunction = MipsBuildingContext.f(getCalledFunction());
        // 先构建出call指令，后续要记录该指令用到的A寄存器
        // ！这也是唯一一次使用野生未封装的new MipsInstruction
        MipsInstruction call;
        // 内建函数，需要宏调用
        if (getCalledFunction().isLibFunc()) {
            call = new MipsMacro(mipsFunction.getName());
            // 系统调用必然改变 v0, v0加入def
            call.addDefReg(MipsRealReg.V0); // TODO: addDefReg 双参数修改为单参数
        }
        // 非内建函数，直接构建jal指令即可
        else {
            call = new MipsCall(mipsFunction);
        }

        // 进行传参, 遍历所有irValue参数
        int argc = getArgs().size();
        for (int i = 0; i < argc; i++) {
            Value irArg = getArgs().get(i);
            MipsOperand src;
            // 前四个参数存储在a0-3内
            if (i < 4) {
                if (irArg instanceof ConvInst && ((ConvInst)irArg).getFrom() == IntegerType.i8 && Objects.equals(mipsFunction.getName(), "putint")){
                    src = MipsBuilder.buildOperand(irArg, true, MipsBuildingContext.curIrFunction, getParent());
                    Value temp = new ConstInt(255);
                    MipsOperand src2;
                    src2 = MipsBuilder.buildOperand(temp, true, MipsBuildingContext.curIrFunction, getParent());
                    MipsBinary addBinary = MipsBuilder.buildBinary(MipsBinary.BinaryType.AND, src, src, src2, getParent());
                    MipsMove move = MipsBuilder.buildMove(new MipsRealReg("a" + i), src, getParent());
                    // 加入use，保护寄存器分配时不消除move
                    call.addUseReg(addBinary.getDst());
                    call.addUseReg(move.getDst());
                }
                else {
                    src = MipsBuilder.buildOperand(irArg, true, MipsBuildingContext.curIrFunction, getParent());
                    MipsMove move = MipsBuilder.buildMove(new MipsRealReg("a" + i), src, getParent());
                    // 加入use，保护寄存器分配时不消除move
                    call.addUseReg(move.getDst());
                }
            }
            // 后面的参数先存进寄存器里，再store进内存
            else {
                // 要求存入寄存器
                src = MipsBuilder.buildOperand(irArg, false, MipsBuildingContext.curIrFunction, getParent());
                // 存入 SP - 4 * nowNum 处
                MipsImm offsetOperand = new MipsImm(-(argc - i) * 4);
                MipsBuilder.buildStore(src, MipsRealReg.SP, offsetOperand, getParent());
            }
        }

        // 栈的生长
        if (argc > 4) {
            // 向下生长4 * allNum: SP = SP - 4 * allNum
            MipsOperand offsetOperand = MipsBuilder.buildImmOperand(4 * (argc - 4), true, MipsBuildingContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, MipsRealReg.SP, MipsRealReg.SP, offsetOperand, getParent());
        }

        // 参数准备妥当后，再执行jal指令
        mipsBlock.addInstruction(call);

        // 这条语句执行完成的场合，恰是从函数中返回
        // 栈的恢复 与生长相反，做加法即可
        if (argc > 4) {
            MipsOperand offsetOperand = MipsBuilder.buildImmOperand(4 * (argc - 4), true, MipsBuildingContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, MipsRealReg.SP, MipsRealReg.SP, offsetOperand, getParent());
        }

        // 因为寄存器分配是以函数为单位的，所以相当于 call 指令只需要考虑在调用者函数中的影响
        // 那么 call 对应的 bl 指令会修改 lr 和 r0 (如果有返回值的话)
        // 此外，r0 - r3 是调用者保存的寄存器，这会导致可能需要额外的操作 mov ，所以这边考虑全部弄成被调用者保存
        for (int i = 0; i < 4; i++) {
            call.addDefReg(new MipsRealReg("a" + i));
        }
        // 非内建函数需要保存返回地址 ra
        if (!getCalledFunction().isLibFunc()) {
            call.addDefReg(MipsRealReg.RA);
        }
        // 处理返回值
        // 调用者应当保存 v0，无论有没有返回值
        Type returnType = ((FunctionType)getCalledFunction().getType()).getReturnType();
        call.addDefReg(MipsRealReg.V0);
        // 带有返回值，则需要记录该返回值
        if (!(returnType instanceof VoidType)) {
            MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());
            MipsBuilder.buildMove(dst, MipsRealReg.V0, getParent());
        }
    }
}
