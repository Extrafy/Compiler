package ir.values.instructions.mem;

import ir.types.Type;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.instructions.Operator;

import java.util.List;

public class PhiInst extends MemInst{

    public PhiInst(BasicBlock block, Type type, List<Value> valueList) {
        super(type, Operator.Phi, block);
        for (Value value : valueList){
            this.addOperand(value);
        }
        this.setName("%" + REG_NUM++);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getName()).append(" = phi ").append(getType()).append(" ");
        for (int i = 0; i < getOperands().size(); i++) {
            s.append("[ ")
                    .append(getOperands().get(i).getName())
                    .append(", %")
                    .append(this.getNode()
                            .getParent()
                            .getValue()
                            .getPredecessors()
                            .get(i)
                            .getName())
                    .append(" ]");
            if (i != getOperands().size() - 1) {
                s.append(", ");
            }
        }
        return s.toString();
    }

    // 直接跳过生成，最后手动插入
    public void buildMips() {
    }

    public void addIncoming(Value value, BasicBlock currentBlock) {
    }

}
