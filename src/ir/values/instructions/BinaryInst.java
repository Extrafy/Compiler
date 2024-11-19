package ir.values.instructions;

import ir.types.IntegerType;
import ir.types.Type;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.BuildFactory;
import ir.values.Value;

public class BinaryInst extends Instruction{

    public BinaryInst(BasicBlock block, Operator op , Value leftValue, Value rightValue) {
        super(VoidType.voidType, op, block);
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
}
