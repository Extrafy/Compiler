package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsBuildingContext;
import backend.MipsUtils.MipsMath;
import backend.instructions.MipsBinary;
import backend.instructions.MipsCondType;
import backend.instructions.MipsMoveHI;
import backend.instructions.MipsShift;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.parts.MipsBlock;
import ir.types.IntegerType;
import ir.types.Type;
import ir.types.VoidType;
import ir.values.*;

public class BinaryInst extends Instruction{

    private BasicBlock belongBlock;
    public BinaryInst(BasicBlock block, Operator op , Value leftValue, Value rightValue) {
        super(VoidType.voidType, op, block);
        this.belongBlock = block;
        // ???可能需要修改
        boolean leftIsI1 = leftValue.getType() instanceof IntegerType && ((IntegerType) leftValue.getType()).isI1();
        boolean rightIsI1 = rightValue.getType() instanceof IntegerType && ((IntegerType) rightValue.getType()).isI1();
        boolean leftIsI8 = leftValue.getType() instanceof IntegerType && ((IntegerType) leftValue.getType()).isI8();
        boolean rightIsI8 = rightValue.getType() instanceof IntegerType && ((IntegerType) rightValue.getType()).isI8();
        boolean leftIsI32 = leftValue.getType() instanceof IntegerType && ((IntegerType) leftValue.getType()).isI32();
        boolean rightIsI32 = rightValue.getType() instanceof IntegerType && ((IntegerType) rightValue.getType()).isI32();
        if (leftIsI1 && rightIsI32) {
            addOperands(BuildFactory.getInstance().buildZext(leftValue, block, IntegerType.i1, IntegerType.i32), rightValue);
        }
        else if (leftIsI32 && rightIsI1) {
            addOperands(leftValue, BuildFactory.getInstance().buildZext(rightValue, block, IntegerType.i1, IntegerType.i32));
        }
        else if (leftIsI8 && rightIsI32){
            addOperands(BuildFactory.getInstance().buildZext(leftValue, block, IntegerType.i8, IntegerType.i32), rightValue);
        }
        else if (leftIsI32 && rightIsI8){
            addOperands(leftValue, BuildFactory.getInstance().buildZext(rightValue, block, IntegerType.i8, IntegerType.i32));
        }
        else if (leftIsI8 && rightIsI8){
            addOperands(BuildFactory.getInstance().buildZext(leftValue, block, IntegerType.i8, IntegerType.i32), BuildFactory.getInstance().buildZext(rightValue, block, IntegerType.i8, IntegerType.i32));
        }
        else {
            addOperands(leftValue, rightValue);
        }
        this.setType(this.getOperands().get(0).getType());
        if (isCond()) {
            this.setType(IntegerType.i1);
        }
        this.setName("%" + REG_NUM++);
    }

    private void addOperands(Value left, Value right) {
        this.addOperand(left);
        this.addOperand(right);
    }

    public boolean isAdd() {
        return this.getOperator() == Operator.Add;
    }

    public boolean isSub() {
        return this.getOperator() == Operator.Sub;
    }

    public boolean isMul() {
        return this.getOperator() == Operator.Mul;
    }

    public boolean isDiv() {
        return this.getOperator() == Operator.Div;
    }

    public boolean isMod() {
        return this.getOperator() == Operator.Mod;
    }

    public boolean isShl() {
        return this.getOperator() == Operator.Shl;
    }

    public boolean isShr() {
        return this.getOperator() == Operator.Shr;
    }

    public boolean isAnd() {
        return this.getOperator() == Operator.And;
    }

    public boolean isOr() {
        return this.getOperator() == Operator.Or;
    }

    public boolean isLt() {
        return this.getOperator() == Operator.Lt;
    }

    public boolean isLe() {
        return this.getOperator() == Operator.Le;
    }

    public boolean isGe() {
        return this.getOperator() == Operator.Ge;
    }

    public boolean isGt() {
        return this.getOperator() == Operator.Gt;
    }

    public boolean isEq() {
        return this.getOperator() == Operator.Eq;
    }

    public boolean isNe() {
        return this.getOperator() == Operator.Ne;
    }

    public boolean isCond() {
        return this.isLt() || this.isLe() || this.isGe() || this.isGt() || this.isEq() || this.isNe();
    }

    public boolean isNot() {
        return this.getOperator() == Operator.Not;
    }

    public String toString() {
        String s = getName() + " = ";
        switch (this.getOperator()) {
            case Add:
                s += "add ";
                s += this.getOperands().get(0).getType().toString();
                s += " ";
                break;
            case Sub:
                s += "sub ";
                s += this.getOperands().get(0).getType().toString();
                s += " ";
                break;
            case Mul:
                s += "mul ";
                s += this.getOperands().get(0).getType().toString();
                s += " ";
                break;
            case Div:
                s += "sdiv ";
                s += this.getOperands().get(0).getType().toString();
                s += " ";
                break;
            case Mod:
//                System.out.println(this.getOperands().get(0));
//                System.out.println(this.getOperands().get(1));
                s += "srem ";
                s += this.getOperands().get(0).getType().toString();
                s += " ";
                break;
            case Shl:
                s += "shl ";
                s += this.getOperands().get(0).getType().toString();
                s += " ";
                break;
            case Shr:
                s += "ashr ";
                s += this.getOperands().get(0).getType().toString();
                s += " ";
                break;
            case And:
                s += "and " + this.getOperands().get(0).getType().toString() + " ";
                break;
            case Or:
                s += "or " + this.getOperands().get(0).getType().toString() + " ";
                break;
            case Lt:
                s += "icmp slt " + this.getOperands().get(0).getType().toString() + " ";
                break;
            case Le:
                s += "icmp sle " + this.getOperands().get(0).getType().toString() + " ";
                break;
            case Ge:
                s += "icmp sge " + this.getOperands().get(0).getType().toString() + " ";
                break;
            case Gt:
                s += "icmp sgt " + this.getOperands().get(0).getType().toString() + " ";
                break;
            case Eq:
                s += "icmp eq " + this.getOperands().get(0).getType().toString() + " ";
                break;
            case Ne:
                s += "icmp ne " + this.getOperands().get(0).getType().toString() + " ";
                break;
            default:
                break;
        }
        return s + this.getOperands().get(0).getName() + ", " + this.getOperands().get(1).getName();
    }

    public void buildMips(){
        switch (this.getOperator()){
            case Add -> {
                MipsBlock block = MipsBuildingContext.b(getParent());
                Value op1 = this.getOperands().get(0), op2 = this.getOperands().get(1);

                MipsOperand src1, src2;
                MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());

                // op1 op2均为常数 则直接move dst Imm(op1+op2)
                if ((op1 instanceof ConstInt || op1 instanceof ConstChar) && (op2 instanceof ConstInt || op2 instanceof ConstChar)) {
                    int imm1 = 0;
                    if (op1 instanceof ConstInt) {
                        imm1 = ((ConstInt)op1).getValue();
                    }
                    else imm1 = ((ConstChar)op1).getValue();
                    int imm2 = 0;
                    if (op2 instanceof ConstInt) {
                        imm2 = ((ConstInt)op2).getValue();
                    }
                    else imm2 = ((ConstChar)op2).getValue();
                    MipsBuilder.buildMove(dst, new MipsImm(imm1 + imm2), getParent());
                }
                // op1 是常数，op2不是常数
                //  则op2应当作为rs, op1的值作为imm，二者需要互换位置
                else if (op1 instanceof ConstInt) {
                    src1 = MipsBuilder.buildOperand(op2, false, MipsBuildingContext.curIrFunction, getParent());
                    src2 = MipsBuilder.buildOperand(op1, true, MipsBuildingContext.curIrFunction, getParent());
                    MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, src1, src2, getParent());
                }
                else if (op1 instanceof ConstChar){
                    src1 = MipsBuilder.buildOperand(op2, false, MipsBuildingContext.curIrFunction, getParent());
                    src2 = MipsBuilder.buildOperand(op1, true, MipsBuildingContext.curIrFunction, getParent());
                    MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, src1, src2, getParent());
                }
                // op1不为常数的场合 在buildOperand内要求op1不为常数即可
                else {
                    src1 = MipsBuilder.buildOperand(op1, false, MipsBuildingContext.curIrFunction, getParent());
                    src2 = MipsBuilder.buildOperand(op2, true, MipsBuildingContext.curIrFunction, getParent());
                    MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, src1, src2, getParent());
                }
            }
            case Mul -> {
                MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());
                Value op1 = this.getOperands().get(0), op2 = this.getOperands().get(1);
                MipsBuilder.buildOptMul(dst, op1, op2, MipsBuildingContext.curIrFunction, getParent());
            }
            case Div -> {
                MipsOperand src1 = MipsBuilder.buildOperand(this.getOperands().get(0), false, MipsBuildingContext.curIrFunction, getParent());
                MipsBlock mipsBlock = MipsBuildingContext.b(getParent());

                MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());
                Value op2 = this.getOperands().get(1);

                // 除数是常数，可以进行常数优化
                if (op2 instanceof ConstInt || op2 instanceof ConstChar) {
                    // 获得除数常量
                    int imm = 0;
                    if (op2 instanceof ConstInt) {
                        imm = ((ConstInt) op2).getValue();
                    }
                    else imm = ((ConstChar) op2).getValue();
                    // 除数为 1 ，无需生成中间代码，,将 ir 映射成被除数，直接记录即可
                    if (imm == 1) {
                        MipsBuildingContext.addOperandMapping(this, src1);
                    }
                    // 除数不为1
                    else {
                        MipsOperand result = MipsBuildingContext.div(mipsBlock, src1, new MipsImm(imm));
                        // 如果先前已有计算结果，无需生成中间代码，直接记录映射即可
                        if (result != null) {
                            MipsBuildingContext.addOperandMapping(this, result);
                        }
                        // 先前没有计算结果，需要手动进行计算
                        else {
                            doDivConstOpt(dst, src1, imm);
                        }
                    }
                }
                // 无法常数优化, 直接进行除法
                else {
                    MipsOperand src2 = MipsBuilder.buildOperand(op2, false, MipsBuildingContext.curIrFunction, getParent());
                    MipsBuilder.buildBinary(MipsBinary.BinaryType.DIV, dst, src1, src2, getParent());
                }
            }

            case Mod -> {
//                System.out.println(this.getOperands());
//                System.out.println(this.getOperands().get(1).toString());
//                if(((ConstInt) this.getOperands().get(1)).getValue() != 1){
//                    System.out.println("[Srem.buildMips] 取余时y不等于1，计算错误");// 取余优化之后
//                }
                MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());
                MipsBuilder.buildMove(dst, MipsRealReg.ZERO, getParent());
            }

            case Sub -> {
                MipsBlock block = MipsBuildingContext.b(getParent());
                Value op1 = this.getOperands().get(0), op2 = this.getOperands().get(1);

                MipsOperand src1, src2;
                MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());

                // op1 op2均为常数 则直接move dst Imm(op1+op2)
                if ((op1 instanceof ConstInt || op1 instanceof ConstChar) && (op2 instanceof ConstInt || op2 instanceof ConstChar)) {
                    int imm1 = 0;
                    if (op1 instanceof ConstInt) {
                        imm1 = ((ConstInt)op1).getValue();
                    }
                    else imm1 = ((ConstChar)op1).getValue();
                    int imm2 = 0;
                    if (op2 instanceof ConstInt) {
                        imm2 = ((ConstInt)op2).getValue();
                    }
                    else imm2 = ((ConstChar)op2).getValue();
                    MipsBuilder.buildMove(dst, new MipsImm(imm1 - imm2), getParent());
                }
                // op2 是常数，op1不是常数
                // 借用addiu 代替：op1 + (-op2)
                // 需要手动调用buildOperand内的一个分支，将-imm2送进去，以构造-op2
                else if (op2 instanceof ConstInt) {
                    int imm2 = ((ConstInt)op2).getValue();
                    src1 = MipsBuilder.buildOperand(op1, false, MipsBuildingContext.curIrFunction, getParent());
                    src2 = MipsBuilder.buildImmOperand(-imm2, true, MipsBuildingContext.curIrFunction, getParent());
                    MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, src1, src2, getParent());
                }
                else if (op2 instanceof ConstChar) {
                    int imm2 = ((ConstChar)op2).getValue();
                    src1 = MipsBuilder.buildOperand(op1, false, MipsBuildingContext.curIrFunction, getParent());
                    src2 = MipsBuilder.buildImmOperand(-imm2, true, MipsBuildingContext.curIrFunction, getParent());
                    MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, src1, src2, getParent());
                }
                // op2不为常数的场合 要求op1不为常数即可
                // 若op1为常数，buildOperand会处理转化为虚拟寄存器
                else {
                    src1 = MipsBuilder.buildOperand(op1, false, MipsBuildingContext.curIrFunction, getParent());
                    src2 = MipsBuilder.buildOperand(op2, true, MipsBuildingContext.curIrFunction, getParent());
                    MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, src1, src2, getParent());
                }
            }
        }
    }

    public void buildMips1() {
        switch (this.getOperator()){
            case Lt,Le,Gt,Ge,Eq,Ne ->{
                MipsCondType mipsCondType = MipsCondType.IrCondType2MipsCondType(getOperator());
                Value op1 = this.getOperands().get(0), op2 = this.getOperands().get(1);
                // 均为常数的场合，我们直接进行比较，得到结果（0或者1）即可
                // 比较的结果生成MipsImm并记录，不必生成目标代码
                if(op1 instanceof ConstInt && op2 instanceof ConstInt){
                    int val1 = ((ConstInt)op1).getValue();
                    int val2 = ((ConstInt)op2).getValue();
                    MipsOperand mipsOperand = new MipsImm(MipsCondType.doCondCalculation(mipsCondType, val1, val2));
                    MipsBuildingContext.addOperandMapping(this, mipsOperand);
                }
                else if(op1 instanceof ConstChar && op2 instanceof ConstChar){
                    int val1 = ((ConstChar)op1).getValue();
                    int val2 = ((ConstChar)op2).getValue();
                    MipsOperand mipsOperand = new MipsImm(MipsCondType.doCondCalculation(mipsCondType, val1, val2));
                    MipsBuildingContext.addOperandMapping(this, mipsOperand);
                }
                // 存在非常数
                else{
                    MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsBuildingContext.curIrFunction, getParent());
                    MipsOperand src1 = MipsBuilder.buildOperand(op1, false, MipsBuildingContext.curIrFunction, getParent());
                    MipsOperand src2 = MipsBuilder.buildOperand(op2, false, MipsBuildingContext.curIrFunction, getParent());
                    MipsBuilder.buildCompare(mipsCondType, dst, src1, src2, getParent());
                }
            }
        }
    }

    private void doDivConstOpt(MipsOperand dst, MipsOperand src, int imm) {
//        System.out.println("imm: " + imm);
        // 这里之所以取 abs，是在之后如果是负数，那么会有一个取相反数的操作
        int abs = Math.abs(imm);
        // 除数为-1，取相反数dst = 0 - src, 生成结束
        if (imm == -1) {
            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsRealReg.ZERO, src,getParent());
            return;
        }
        // 除数为1，直接进行move
        else if (imm == 1) {
            MipsBuilder.buildMove(dst, src, getParent());
        }
        // 如果是 2 的幂次
        else if (MipsMath.isPow2(abs)) {
            // 末尾0的个数
            int l = MipsMath.countTailZeroNumber(abs);
            // 产生新的被除数
            MipsOperand newSrc = buildNegativeSrcCeil(src, abs);
            // 将新的被除数右移
            MipsBuilder.buildShift(MipsShift.ShiftType.SRA, dst, newSrc, l, getParent());
        }
        // 转换公式dst = src / abs
        // dst = (src * n) >> shift
        else {
            long nc = ((long) 1 << 31) - (((long) 1 << 31) % abs) - 1;
            long p = 32;
            while (((long) 1 << p) <= nc * (abs - ((long) 1 << p) % abs)) {
                p++;
            }
            long m = ((((long) 1 << p) + (long) abs - ((long) 1 << p) % abs) / (long) abs);
            int n = (int) ((m << 32) >>> 32);
            int shift = (int) (p - 32);

            // tmp0 = n
            MipsOperand tmp0 = MipsBuilder.allocVirtualReg(MipsBuildingContext.curIrFunction);
            MipsBuilder.buildMove(tmp0, new MipsImm(n), getParent());

            MipsOperand tmp1 = MipsBuilder.allocVirtualReg(MipsBuildingContext.curIrFunction);
            // tmp1 = src + (src * n)[63:32]
            if (m >= 0x80000000L) {
                // HI = src
                MipsBuilder.buildMoveHI(MipsMoveHI.MoveHIType.MTHI, src, getParent());
                // tmp1 += src * tmp0 + HI （有符号乘法）
                MipsBuilder.buildBinary(MipsBinary.BinaryType.SMMADD, tmp1, src, tmp0, getParent());
            }
            // tmp1 = (src * n)[63:32] 有符号的
            else {
                MipsBuilder.buildBinary(MipsBinary.BinaryType.SMMUL, tmp1, src, tmp0, getParent());
            }

            MipsOperand tmp2 = MipsBuilder.allocVirtualReg(MipsBuildingContext.curIrFunction);
            // tmp2 = tmp1 >> shift
            MipsBuilder.buildShift(MipsShift.ShiftType.SRA, tmp2, tmp1, shift, getParent());
            // AT = src >> 31
            MipsBuilder.buildShift(MipsShift.ShiftType.SRL, MipsRealReg.AT, src, 31, getParent());
            // dst = tmp2 + AT
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, MipsRealReg.AT, tmp2, getParent());
        }

        // 先前都是使用的除数绝对值abs
        // 如果除数为负值，需要变为相反数
        if (imm < 0) {
            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsRealReg.ZERO, dst, getParent());
        }
        // 记录，以便后续使用
        MipsBuildingContext.addDivMapping(MipsBuildingContext.b(getParent()), src, new MipsImm(imm), dst);
    }

    /**
     * 针对负被除数的除法向上取整，产生新的被除数
     * 若只采用移位操作，除法向下取整 -3 / 4 = -1，与除法的含义不符
     * 新的被除数：newDividend = oldDividend + divisor - 1
     * @param oldSrc        旧的被除数
     * @param absImm         除数的绝对值，为2的幂次
     * @return 新的被除数
     */
    private MipsOperand buildNegativeSrcCeil(MipsOperand oldSrc, int absImm) {
        MipsOperand newSrc = MipsBuilder.allocVirtualReg(MipsBuildingContext.curIrFunction);
        int l = MipsMath.countTailZeroNumber(absImm);

        // tmp1 = (oldDividend >> 31)
        // 这是为了保留负数的最高位1，正数在下面的过程中不受影响
        MipsOperand tmp1 = MipsBuilder.allocVirtualReg(MipsBuildingContext.curIrFunction);
        MipsBuilder.buildShift(MipsShift.ShiftType.SRA, tmp1, oldSrc, 31, getParent());
        // tmp1 = tmp1 << 32-l
        // 如果被除数是负数，那么[l-1 : 0] 位全为1，这就是abs - 1
        MipsBuilder.buildShift(MipsShift.ShiftType.SRL, tmp1, tmp1, 32 - l, getParent());
        // newSrc = oldSrc + divisor - 1
        MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, newSrc, oldSrc, tmp1, getParent());
        return newSrc;
    }
}
