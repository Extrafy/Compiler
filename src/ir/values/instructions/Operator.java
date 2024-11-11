package ir.values.instructions;

public enum Operator {
    // 二元运算符
    Add, Sub, Mul, Div, Mod, Shl, Shr, And, Or,

    // 关系运算符
    Lt, Le, Gt, Ge, Eq, Ne,

    // 类型转换
    Trunc, Zext, Bitcast,  // zext..to 和 trunc..to

    // 内存操作
    Alloca, Load, Store, GEP, // GEP: getelementptr

    // Phi 指令
    Phi, MemPhi, LoadDep,

    // 跳转指令
    Br, Call, Ret,

    // 非运算符
    Not

}
