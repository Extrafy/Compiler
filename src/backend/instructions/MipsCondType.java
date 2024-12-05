package backend.instructions;

import ir.values.instructions.Operator;

import java.util.HashMap;

public enum MipsCondType {
    EQ("==", "eq"),
    NE("!=", "ne"),
    LE("<=", "le"),
    LT("<", "lt"),
    GE(">=", "ge"),
    GT(">", "gt");

    private String meaning;
    private String name;

    MipsCondType(String meaning, String name) {
        this.meaning = meaning;
        this.name = name;
    }

    private static final HashMap<Operator, MipsCondType> ir2mips = new HashMap<>() {{
        put(Operator.Eq, MipsCondType.EQ);
        put(Operator.Ne, MipsCondType.NE);
        put(Operator.Ge, MipsCondType.GE);
        put(Operator.Gt, MipsCondType.GT);
        put(Operator.Le, MipsCondType.LE);
        put(Operator.Lt, MipsCondType.LT);
    }};

    // 将中间代码中的Operator类转化为Mips的CondType类
    public static MipsCondType IrCondType2MipsCondType(Operator type) {
        return ir2mips.get(type);
    }

    public static int doCondCalculation(MipsCondType type, int src1, int src2) {
        boolean result = false;
        switch (type) {
            case EQ -> result = src1 == src2;
            case NE -> result = src1 != src2;
            case GE -> result = src1 >= src2;
            case GT -> result = src1 > src2;
            case LE -> result = src1 <= src2;
            case LT -> result = src1 < src2;
        }
        return result ? 1 : 0;
    }

    // 获取与当前type相反的type
    public static MipsCondType getOppCondType(MipsCondType type) {
        switch (type) {
            case EQ: {
                return NE;
            }
            case LE: {
                return GT;
            }
            case LT: {
                return GE;
            }
            case GE: {
                return LT;
            }
            case GT: {
                return LE;
            }
            case NE: {
                return EQ;
            }
            default: {
                return null;
            }
        }
    }

    public String toString() {
        return name;
    }
}

