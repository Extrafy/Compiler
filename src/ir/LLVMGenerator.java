package ir;

import AST.AST;
import config.Config;
import frontend.Parser;
import ir.opt.*;
import ir.types.*;
import ir.values.*;
import ir.values.instructions.ConstArray;
import ir.values.instructions.Operator;
import token.TokenType;

import java.util.*;

public class LLVMGenerator {
    private static final LLVMGenerator llvmGenerator = new LLVMGenerator();

    public static LLVMGenerator getInstance(){
        return llvmGenerator;
    }

    public void process() {
        // ============ 生成中间代码 ============
        LLVMGenerator.getInstance().visitCompUnit(Parser.getInstance().getAst().compUnit);  // 生成中间代码
        // 优化
        if (Config.openDeadCodeAndLoopOpt){
            DeadCodeRemove.analyze();           // 死代码删除
            // ControlFlowGraphAnalyzer.analyze(); // 控制流图构建 // TLE
            DomainTreeAnalyzer.analyze();       // domain树生成
            LoopAnalyzer.analyze();             // 循环分析
        }
        if (Config.openMem2RegOpt) {
            new Mem2Reg().analyze();           // Mem2Reg
        }
    }

    private BuildFactory buildFactory = BuildFactory.getInstance();

    // 记录当前会用到的block
    private BasicBlock curBlock = null;
    private BasicBlock curTrueBlock = null;
    private BasicBlock curFalseBlock = null;
    private BasicBlock continueBlock = null;
    private BasicBlock curForFinalBlock = null;
    private Function curFunction = null;

    // 计算时的中间变量
    private Integer saveValue = null;  // 初始化的常数
    private Integer saveCharValue = null;
    private Operator saveOp = null;
    private Operator tmpOp = null;
    private Type tmpType = null;
    private Value tmpValue = null;
    private int tmpIndex = 0;
    private List<Value> tmpValueList = null;
    private List<Type> tmpTypeList = null;
    private List<Value> funcArgs = null;

    private boolean isGlobal = true;
    private boolean isConst = false;
    private boolean isArray = false;
    private boolean isRegister = false;

    // 数组
    private Value curArray = null;
    private String tmpName = null;
    private int tmpDepth = 0;
    private int tmpOffset = 0;
    private List<Integer> tmpDims = null;

    // 重构符号表
    private List< Map<String, Value> > symbolTable = new ArrayList<>();

    public Map<String, Value> getCurSymbolTable() {
        return symbolTable.get(symbolTable.size() - 1);
    }

    public void addSymbol(String name, Value value) {
        getCurSymbolTable().put(name, value);
    }

    public void addGlobalSymbol(String name, Value value) {
        symbolTable.get(0).put(name, value);
    }

    public Value getValue(String name) {
        for (int i = symbolTable.size() - 1; i >= 0; i--) {
            if (symbolTable.get(i).containsKey(name)) {
                return symbolTable.get(i).get(name);
            }
        }
        return null;
    }

    public void addSymbolTable() {
        symbolTable.add(new HashMap<>());
    }

    public void removeSymbolTable() {
        symbolTable.remove(symbolTable.size() - 1);
    }

    // 常量表
    private List< Map<String, Integer> > constTable = new ArrayList<>();

    public Map<String, Integer> getCurConstTable() {
        return constTable.get(constTable.size() - 1);
    }

    public void addConst(String name, Integer value) {
        getCurConstTable().put(name, value);
    }

    public Integer getConst(String name) {
        for (int i = constTable.size() - 1; i >= 0; i--) {
            if (constTable.get(i).containsKey(name)) {
                return constTable.get(i).get(name);
            }
        }
        return 0;
    }

    public void changeConst(String name, Integer value) {
        for (int i = constTable.size() - 1; i >= 0; i--) {
            if (constTable.get(i).containsKey(name)) {
                constTable.get(i).put(name, value);
                return;
            }
        }
    }

    public int calculateConst(Operator op, int a, int b) {
        return switch (op) {
            case Add -> a + b;
            case Sub -> a - b;
            case Mul -> a * b;
            case Div -> a / b;
            case Mod -> a % b;
            default -> 0;
        };
    }

    public void addConstTable() {
        constTable.add(new HashMap<>());
    }

    public void removeConstTable() {
        constTable.remove(constTable.size() - 1);
    }

    // 字符串处理
    private List<String> stringList = new ArrayList<>();

    private int getStringIndex(String str) {
        for (int i = 0; i < stringList.size(); i++) {
            if (stringList.get(i).equals(str)) {
                return i;
            }
        }
        stringList.add(str);
        Type type = buildFactory.getArrayType(IntegerType.i8, str.length() + 1);
        Value value = buildFactory.buildGlobalVar(getStringName(str), type, true, buildFactory.getConstString(str));
        addGlobalSymbol(getStringName(str), value);
        return stringList.size() - 1;
    }

    private String getStringName(int index) {
        return "_str_" + index;
    }

    private String getStringName(String str) {
        return getStringName(getStringIndex(str));
    }

    // 遍历语法树AST
    public void visitCompUnit(AST.CompUnit compUnit){
        // init
        addSymbolTable();
        addConstTable();
        addSymbol("getint", buildFactory.buildLibraryFunction("getint", IntegerType.i32, new ArrayList<>()));
        addSymbol("getchar", buildFactory.buildLibraryFunction("getchar", IntegerType.i32, new ArrayList<>()));
        addSymbol("putint", buildFactory.buildLibraryFunction("putint", VoidType.voidType, new ArrayList<>(Collections.singleton(IntegerType.i32))));
        addSymbol("putchar", buildFactory.buildLibraryFunction("putchar", VoidType.voidType, new ArrayList<>(Collections.singleton(IntegerType.i32))));
        addSymbol("putstr", buildFactory.buildLibraryFunction("putstr", VoidType.voidType, new ArrayList<>(Collections.singleton(new PointerType(IntegerType.i8)))));

        // CompUnit → {Decl} {FuncDef} MainFuncDef
        for (AST.Decl decl : compUnit.getDeclList()){
            visitDecl(decl);
        }
        for (AST.FuncDef funcDef: compUnit.getFuncDefList()){
            visitFuncDef(funcDef);
        }
        visitMainFuncDef(compUnit.getMainFuncDef());
    }

    public void visitDecl(AST.Decl decl){
        // Decl → ConstDecl | VarDecl
        if (decl.getConstDecl() != null){
            visitConstDecl(decl.getConstDecl());
        }
        else visitVarDecl(decl.getVarDecl());
    }

    public void visitConstDecl(AST.ConstDecl constDecl){
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        if (constDecl.getbType().getIntToken() != null){
            tmpType = IntegerType.i32;
        }
        else tmpType = IntegerType.i8;
        for (AST.ConstDef constDef : constDecl.getConstDefList()){
            visitConstDef(constDef);
        }
    }

    public void visitConstDef(AST.ConstDef constDef){
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        String name = constDef.getIdent().getValue();
        if (constDef.getConstExp() == null){
            // 不是数组
            visitConstInitVal(constDef.getConstInitVal());
            if (tmpType == IntegerType.i8){
                tmpValue = buildFactory.getConstInt(saveValue == null ? 0 : saveValue, IntegerType.i8);
            }
            else {
                tmpValue = buildFactory.getConstInt(saveValue == null ? 0 : saveValue, IntegerType.i32);
            }
            addConst(name, saveValue);
            if (isGlobal){
                tmpValue = buildFactory.buildGlobalVar(name, tmpType, true, tmpValue);
                addSymbol(name, tmpValue);
            }
            else {
                tmpValue = buildFactory.buildVar(curBlock, tmpValue, true, tmpType);
                addSymbol(name, tmpValue);
            }
        }
        else {
            // 数组
            List<Integer> dims = new ArrayList<>();
            visitConstExp(constDef.getConstExp());
            dims.add(saveValue);
            tmpDims = new ArrayList<>(dims);
            Type type = buildFactory.getArrayType(tmpType, dims.get(0));
//            for (int i = dims.size() - 1; i >= 0; i--){
//                if (type == null){
//                    type = buildFactory.getArrayType(tmpType, dims.get(i));
//                }
//                else type = buildFactory.getArrayType(type, dims.get(i));
//            }
            if (isGlobal){
                tmpValue = buildFactory.buildGlobalArray(name, type, true);
                ((ConstArray)((GlobalVar) tmpValue).getValue()).setInit(true);
            }
            else {
                tmpValue = buildFactory.buildArray(curBlock, true, type);
            }
            addSymbol(name, tmpValue);
            curArray = tmpValue;
            isArray = true;
            tmpName = name;
            tmpOffset = 0;
            tmpDepth = 0;
            visitConstInitVal(constDef.getConstInitVal());
            isArray = false;
        }
    }

    public void visitConstInitVal(AST.ConstInitVal constInitVal){
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if (constInitVal.getStringConst() != null){
            String str = constInitVal.getStringConst().getValue();
            tmpDepth = 1;
            for (int i = 1; i < str.length() - 1; i++){
                int ascii = (int)str.charAt(i); // '\n'
                if (ascii == 92){
                    ascii = 10;
                    i++;
                }
                tmpValue = buildFactory.getConstInt(ascii, IntegerType.i8);
                if (isGlobal){
                    buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                }
                else {
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                }
                StringBuilder name = new StringBuilder(tmpName);
                List<Value> args = ((ArrayType) ((PointerType) curArray.getType()).getTargetType()).offsetToIndex(tmpOffset);
                for (Value v : args) {
                    name.append(((ConstInt) v).getValue()).append(";");
                }
                addConst(name.toString(), saveValue);
                tmpOffset ++;
            }
        }
        else if (constInitVal.getConstExpList().size() == 1 && !isArray){
            // ConstExp
            visitConstExp(constInitVal.getConstExpList().get(0));
        }
        else {
            // 数组 '{' [ ConstExp { ',' ConstExp } ] '}'
            if (constInitVal.getConstExpList().size() == 1){
                tmpValue = null;
                visitConstExp(constInitVal.getConstExpList().get(0));
                tmpDepth = 1;
                if (tmpType == IntegerType.i8){
                    tmpValue = buildFactory.getConstInt(saveValue, IntegerType.i8);
                }
                else tmpValue = buildFactory.getConstInt(saveValue, IntegerType.i32);
                if (isGlobal){
                    buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                }
                else {
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                }
                StringBuilder name = new StringBuilder(tmpName);
                List<Value> args = ((ArrayType) ((PointerType) curArray.getType()).getTargetType()).offsetToIndex(tmpOffset);
                for (Value v : args) {
                    name.append(((ConstInt) v).getValue()).append(";");
                }
                addConst(name.toString(), saveValue);
                tmpOffset++;
            }
            else if (constInitVal.getConstExpList().size() > 1){
                tmpDepth = 1;
                for (AST.ConstExp constExp : constInitVal.getConstExpList()){
                    visitConstExp(constExp);
                    if (tmpType == IntegerType.i8){
                        tmpValue = buildFactory.getConstInt(saveValue, IntegerType.i8);
                    }
                    else tmpValue = buildFactory.getConstInt(saveValue, IntegerType.i32);
                    if (isGlobal){
                        buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                    }
                    else {
                        buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                    }
                    StringBuilder name = new StringBuilder(tmpName);
                    List<Value> args = ((ArrayType) ((PointerType) curArray.getType()).getTargetType()).offsetToIndex(tmpOffset);
                    for (Value v : args) {
                        name.append(((ConstInt) v).getValue()).append(";");
                    }
                    addConst(name.toString(), saveValue);
                    tmpOffset ++;
                }
//                int size = 1;
//                for (int i = 1; i < depth; i++) {
//                    size *= tmpDims.get(tmpDims.size() - i);
//                }
//                tmpOffset = Math.max(tmpOffset, offset + size);
            }
        }
    }

    public void visitVarDecl(AST.VarDecl varDecl){
        // VarDecl → BType VarDef { ',' VarDef } ';'
        if (varDecl.getbType().getIntToken() != null){
            tmpType = IntegerType.i32;
        }
        else {
            tmpType = IntegerType.i8;
        }
        for (AST.VarDef varDef : varDecl.getVarDefList()){
            visitVarDef(varDef);
        }
    }

    public void visitVarDef(AST.VarDef varDef){
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        String name = varDef.getIdent().getValue();
        if (varDef.getConstExp() == null){
            // 非数组
            if (varDef.getInitVal() != null){
                tmpValue = null;
                if (isGlobal){
                    isConst = true;
                    saveValue = null;
                }
                visitInitVal(varDef.getInitVal());
                isConst = false;
            }
            else {
                tmpValue = null;
                if (isGlobal){
                    saveValue = null;
                }
            }
            if (isGlobal){
                if (tmpType == IntegerType.i8){
                    tmpValue = buildFactory.buildGlobalVar(name, tmpType, false, buildFactory.getConstInt(saveValue == null ? 0 : saveValue, IntegerType.i8));
                }
                else tmpValue = buildFactory.buildGlobalVar(name, tmpType, false, buildFactory.getConstInt(saveValue == null ? 0 : saveValue, IntegerType.i32));
                addSymbol(name, tmpValue);
            }
            else {
                tmpValue = buildFactory.buildVar(curBlock, tmpValue, isConst, tmpType);
                addSymbol(name, tmpValue);
            }
        }
        else {
            // 数组
            isConst = true;
            List<Integer> dims = new ArrayList<>();
            visitConstExp(varDef.getConstExp());
            dims.add(saveValue);
            isConst = false;
            tmpDims = new ArrayList<>(dims);
            Type type = null;
            for (int i = dims.size() - 1; i >= 0; i--) {
                if (type == null) {
                    type = buildFactory.getArrayType(tmpType, dims.get(i));
                }
                else {
                    type = buildFactory.getArrayType(type, dims.get(i));
                }
            }
            if (isGlobal){
                tmpValue = buildFactory.buildGlobalArray(name, type, false);
                if (varDef.getInitVal() != null){
                    ((ConstArray) ((GlobalVar) tmpValue).getValue()).setInit(true);
                }
            }
            else tmpValue = buildFactory.buildArray(curBlock, false, type);
            addSymbol(name, tmpValue);
            curArray = tmpValue;
            if (varDef.getInitVal() != null ){
                isArray = true;
                tmpName = name;
                tmpDepth = 0;
                tmpOffset = 0;
                visitInitVal(varDef.getInitVal());
                isArray = false;
            }
            isConst = false;
        }
    }

    public void visitInitVal(AST.InitVal initVal){
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if (initVal.getStringConst() != null){
            String str = initVal.getStringConst().getValue();
            if (isGlobal){
                isConst = true;
            }
            saveValue = null;
            tmpValue = null;
            tmpDepth = 1;
            for (int i = 1; i < str.length() - 1; i++) {
                int ascii = (int)str.charAt(i); // '\n'
                if (ascii == 92){
                    ascii = 10;
                    i++;
                }
                if (isGlobal) {
                    tmpValue = buildFactory.getConstInt(ascii, IntegerType.i8);
                    buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                }
                else {
                    tmpValue = buildFactory.getConstInt(ascii, IntegerType.i8);
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                }

                tmpOffset++;
            }
            isConst = false;
        }
        else if (initVal.getExpList().size() == 1  && !isArray){
            // Exp
            visitExp(initVal.getExpList().get(0));
        }
        else {
            if (initVal.getExpList().size() == 1){
                if (isGlobal){
                    isConst = true;
                }
                saveValue = null;
                tmpValue = null;
                visitExp(initVal.getExpList().get(0));
                isConst = false;
                tmpDepth = 1;
                if (isGlobal){
                    if (tmpType == IntegerType.i8){
                        tmpValue = buildFactory.getConstInt(saveValue, IntegerType.i8);
                    }
                    else tmpValue = buildFactory.getConstInt(saveValue, IntegerType.i32);
                    buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                }
                else {
                    buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                }

                tmpOffset ++;
            }
            else if (initVal.getExpList().size() > 1){
                if (isGlobal){
                    isConst = true;
                }
                saveValue = null;
                tmpValue = null;
                tmpDepth = 1;
//                int depth = 0;
//                int offset = tmpOffset;
                for (AST.Exp exp : initVal.getExpList()){
                    visitExp(exp);
                    if (isGlobal){
                        if (tmpType == IntegerType.i8){
                            tmpValue = buildFactory.getConstInt(saveValue, IntegerType.i8);
                        }
                        else tmpValue = buildFactory.getConstInt(saveValue, IntegerType.i32);
                        buildFactory.buildInitArray(curArray, tmpOffset, tmpValue);
                    }
                    else {
                        buildFactory.buildStore(curBlock, buildFactory.buildGEP(curBlock, curArray, tmpOffset), tmpValue);
                    }

                    tmpOffset ++;
//                    depth = Math.max(depth, tmpDepth);
                }
                isConst = false;
//                depth ++;
//                int size = 1;
//                for (int i = 1; i < depth; i++) {
//                    size *= tmpDims.get(tmpDims.size() - i);
//                }
//                tmpOffset = Math.max(tmpOffset, offset + size);
//                tmpDepth = depth;
            }
        }
    }

    public void visitFuncDef(AST.FuncDef funcDef){
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        isGlobal = false;
        String funcName = funcDef.getIdent().getValue();
        Type type = null;
        if (funcDef.getFuncType().getIntToken() != null){
            type = IntegerType.i32;
        }
        else if (funcDef.getFuncType().getCharToken() != null){
            type = IntegerType.i8;
        }
        else type = VoidType.voidType;
        tmpTypeList = new ArrayList<>();
        if (funcDef.getFuncFParams() != null){
            visitFuncFParams(funcDef.getFuncFParams());
        }
        Function function = buildFactory.buildFunction(funcName, type, tmpTypeList);
        curFunction = function;
        addSymbol(funcName, function);
        addSymbolTable();
        addConstTable();
        addSymbol(funcName, function);
        curBlock = buildFactory.buildBasicBlock(curFunction);
        funcArgs = buildFactory.getFunctionArguments(curFunction);
        isRegister = true;
        if (funcDef.getFuncFParams() != null){
            visitFuncFParams(funcDef.getFuncFParams());
        }
        isRegister = false;
        visitBlock(funcDef.getBlock());
        isGlobal = true;
        removeSymbolTable();
        removeConstTable();
        buildFactory.checkBlockEnd(curBlock);
    }

    public void visitMainFuncDef(AST.MainFuncDef mainFuncDef){
        // MainFuncDef → 'int' 'main' '(' ')' Block
        isGlobal = false;
        Function function = buildFactory.buildFunction("main", IntegerType.i32, new ArrayList<>());
        curFunction = function;
        addSymbol("main", function);
        addSymbolTable();
        addConstTable();
        addSymbol("main", function);
        curBlock = buildFactory.buildBasicBlock(curFunction);
        funcArgs = buildFactory.getFunctionArguments(curFunction);
        visitBlock(mainFuncDef.getBlock());
        isGlobal = true;
        removeSymbolTable();
        removeConstTable();
        buildFactory.checkBlockEnd(curBlock);
    }

    public void visitFuncFParams(AST.FuncFParams funcFParams){
        // FuncFParams → FuncFParam { ',' FuncFParam }
        if (isRegister){
            tmpIndex = 0;
            for (AST.FuncFParam funcFParam : funcFParams.getFuncFParamList()){
                visitFuncFParam(funcFParam);
                tmpIndex++;
            }
        }
        else {
            tmpTypeList = new ArrayList<>();
            for (AST.FuncFParam funcFParam : funcFParams.getFuncFParamList()){
                visitFuncFParam(funcFParam);
                tmpTypeList.add(tmpType);
            }
        }
    }

    public void visitFuncFParam(AST.FuncFParam funcFParam){
        // FuncFParam → BType Ident ['[' ']']
        if (isRegister){
            int i = tmpIndex;
            Value value = buildFactory.buildVar(curBlock, funcArgs.get(i), false, tmpTypeList.get(i));
            addSymbol(funcFParam.getIdent().getValue(), value);
        }
        else {
            if (funcFParam.getlBrackToken() == null){
                tmpType = funcFParam.getbType().getIntToken() != null ? IntegerType.i32 : IntegerType.i8;
            }
            else {
                List<Integer> dims = new ArrayList<>();
                dims.add(-1);
                tmpType = null;
                for (int i = dims.size() - 1; i >= 0; i--) {
                    if (tmpType == null) {
                        tmpType = funcFParam.getbType().getIntToken() != null ? IntegerType.i32 : IntegerType.i8;
                    }
                    tmpType = buildFactory.getArrayType(tmpType, dims.get(i));
                }
            }
        }
    }

    public void visitBlock(AST.Block block){
        // Block → '{' { BlockItem } '}'
        for (AST.BlockItem blockItem : block.getBlockItemList()){
            visitBlockItem(blockItem);
        }
    }

    public void visitBlockItem(AST.BlockItem blockItem){
        // BlockItem → Decl | Stmt
        if (blockItem.getDecl() != null){
            visitDecl(blockItem.getDecl());
        }
        else {
            visitStmt(blockItem.getStmt());
        }
    }

    public void visitStmt(AST.Stmt stmt){
        /*
            Stmt → LVal '=' Exp ';'
                | [Exp] ';'
                | Block
                | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                | 'break' ';' | 'continue' ';'
                | 'return' [Exp] ';'
                | LVal '=' 'getint''('')'';'
                | LVal '=' 'getchar''('')'';'
                | 'printf''('StringConst {','Exp}')'';'
        */
        switch (stmt.getStmtType()){
            case LValAssignExp:
                if (stmt.getlVal().getExp() == null){
                    // 非数组
                    Value input = getValue(stmt.getlVal().getIdent().getValue());  // 查到的value,pointer
                    visitExp(stmt.getExp());
                    if ((((PointerType) input.getType()).getTargetType() instanceof IntegerType) && (((PointerType) input.getType()).getTargetType() == IntegerType.i8) && (tmpValue.getType() == IntegerType.i32)){
                        Value truncValue =  buildFactory.buildTrunc(tmpValue, curBlock, IntegerType.i32, IntegerType.i8);
                        tmpValue = buildFactory.buildStore(curBlock, input, truncValue);
                    }
                    else if ((((PointerType) input.getType()).getTargetType() instanceof IntegerType) && (((PointerType) input.getType()).getTargetType() == IntegerType.i32) && (tmpValue.getType() == IntegerType.i8)){
                        Value zextValue = buildFactory.buildZext(tmpValue, curBlock, IntegerType.i8, IntegerType.i32);
                        tmpValue = buildFactory.buildStore(curBlock, input, zextValue);
                    }
                    else tmpValue = buildFactory.buildStore(curBlock, input, tmpValue);
                }
                else {
                    // 数组
                    List<Value> indexList = new ArrayList<>();
                    visitExp(stmt.getlVal().getExp());
                    indexList.add(tmpValue);

                    tmpValue = getValue(stmt.getlVal().getIdent().getValue());
                    Value addr;
                    Type type = tmpValue.getType();
                    Type targetType = ((PointerType) type).getTargetType();
                    if (targetType instanceof PointerType) {
                        // arr[][3]
                        tmpValue = buildFactory.buildLoad(curBlock, tmpValue);
                    } else {
                        // arr[3][2]
                        indexList.add(0, ConstInt.zero);
                    }
                    addr = buildFactory.buildGEP(curBlock, tmpValue, indexList);
                    visitExp(stmt.getExp());
                    if ((((PointerType) addr.getType()).getTargetType() instanceof IntegerType) && (((PointerType) addr.getType()).getTargetType() == IntegerType.i8) && (tmpValue.getType() == IntegerType.i32)){
                        Value truncValue =  buildFactory.buildTrunc(tmpValue, curBlock, IntegerType.i32, IntegerType.i8);
                        tmpValue = buildFactory.buildStore(curBlock, addr, truncValue);
                    }
                    else if ((((PointerType) addr.getType()).getTargetType() instanceof IntegerType) && (((PointerType) addr.getType()).getTargetType() == IntegerType.i32) && (tmpValue.getType() == IntegerType.i8)){
                        Value zextValue = buildFactory.buildZext(tmpValue, curBlock, IntegerType.i8, IntegerType.i32);
                        tmpValue = buildFactory.buildStore(curBlock, addr, zextValue);
                    }
                    else tmpValue = buildFactory.buildStore(curBlock, addr, tmpValue);
                }
                break;

            case Exp:
                if (stmt.getExp() != null){
                    visitExp(stmt.getExp());
                }
                break;

            case Block:
                addSymbolTable();
                addConstTable();
                visitBlock(stmt.getBlock());
                removeSymbolTable();
                removeConstTable();
                break;

            case If:
                if (stmt.getElseToken() == null){
                    /*
                     basicBlock;
                     if (...) {
                        trueBlock;
                     }
                     finalBlock;
                    */
                    BasicBlock basicBlock = curBlock;
                    BasicBlock trueBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = trueBlock;
                    visitStmt(stmt.getStmtList().get(0));
                    BasicBlock finalBlock = buildFactory.buildBasicBlock(curFunction);
                    buildFactory.buildBr(curBlock, finalBlock);

                    curTrueBlock = trueBlock;
                    curFalseBlock = finalBlock;
                    curBlock = basicBlock;
                    visitCond(stmt.getCond());
                    curBlock = finalBlock;
                }
                else {
                    /*
                     basicBlock;
                     if (...) {
                        trueBlock;
                        ...
                        trueEndBlock;
                     } else {
                        falseBlock;
                        ...
                        falseEndBlock;
                     }
                     finalBlock;
                    */
                    BasicBlock basicBlock = curBlock;

                    BasicBlock trueBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = trueBlock;
                    visitStmt(stmt.getStmtList().get(0));
                    BasicBlock trueEndBlock = curBlock;

                    BasicBlock falseBlock = buildFactory.buildBasicBlock(curFunction);
                    curBlock = falseBlock;
                    visitStmt(stmt.getStmtList().get(1));
                    BasicBlock falseEndBlock = curBlock;

                    curBlock = basicBlock;
                    curTrueBlock = trueBlock;
                    curFalseBlock = falseBlock;
                    visitCond(stmt.getCond());

                    BasicBlock finalBlock = buildFactory.buildBasicBlock(curFunction);
                    buildFactory.buildBr(trueEndBlock, finalBlock);
                    buildFactory.buildBr(falseEndBlock, finalBlock);
                    curBlock = finalBlock;
                }
                break;

            case For:
                /*
                 basicBlock;
                 for (init; condition; step) {
                    forBlock;
                 }
                 forFinalBlock;
                */
                //
                BasicBlock basicBlock = curBlock;
                BasicBlock tmpContinueBlock = continueBlock;
                BasicBlock tmpForFinalBlock = curForFinalBlock;

//              BasicBlock initBlock = buildFactory.buildBasicBlock(curFunction); // ForStmt1
                BasicBlock condBlock = buildFactory.buildBasicBlock(curFunction);  // 条件判断
                BasicBlock loopBlock = buildFactory.buildBasicBlock(curFunction);  // 循环主体
                BasicBlock loopEndBlock = buildFactory.buildBasicBlock(curFunction);   // 循环主题结束的[ForStmt2]
                BasicBlock endBlock = buildFactory.buildBasicBlock(curFunction);   // 下一个基本块
                curForFinalBlock = endBlock;

                // 创建一个块用于初始化
                if (stmt.getForStmt1() != null){
                    visitForStmt(stmt.getForStmt1());
                }
                buildFactory.buildBr(basicBlock, condBlock);

                curBlock = condBlock;
                if (stmt.getCond() != null){
                    curTrueBlock = loopBlock;
                    curFalseBlock = endBlock;
                    visitCond(stmt.getCond());
                }
                else {
                    buildFactory.buildBr(curBlock, loopBlock);
                }

                curBlock = loopEndBlock;
                if (stmt.getForStmt2() != null){
                    visitForStmt(stmt.getForStmt2());
                }
                buildFactory.buildBr(curBlock, condBlock);

                curBlock = loopBlock;
                continueBlock = loopEndBlock;
                visitStmt(stmt.getStmtList().get(0));
                buildFactory.buildBr(curBlock, loopEndBlock);

                continueBlock = tmpContinueBlock;
                curForFinalBlock = tmpForFinalBlock;

                curBlock = endBlock;

                break;

            case Break:
                buildFactory.buildBr(curBlock, curForFinalBlock);
                break;

            case Continue:
                buildFactory.buildBr(curBlock,continueBlock);
                break;

            case Return:
                if (stmt.getExp() == null){
                    buildFactory.buildRet(curBlock);
                }
                else {
                    //tmpType = ((FunctionType)curFunction.getType()).getReturnType();
                    visitExp(stmt.getExp());
//                    System.out.println(tmpValue.toString());
//                    System.out.println(tmpValue.getType());
                    if (tmpValue.getType() == IntegerType.i32 && ((FunctionType)curFunction.getType()).getReturnType() == IntegerType.i8){
                        Value truncValue = buildFactory.buildTrunc(tmpValue, curBlock, IntegerType.i32, IntegerType.i8);
                        buildFactory.buildRet(curBlock,truncValue);
                    }
                    else if (tmpValue.getType() == IntegerType.i8 && ((FunctionType)curFunction.getType()).getReturnType() == IntegerType.i32){
                        Value zextValue = buildFactory.buildZext(tmpValue, curBlock, IntegerType.i8, IntegerType.i32);
                        buildFactory.buildRet(curBlock, zextValue);
                    }
                    else buildFactory.buildRet(curBlock,tmpValue);
                }
                break;

            case LValAssignGetint:
                if (stmt.getlVal().getExp() == null) {
                    Value input = getValue(stmt.getlVal().getIdent().getValue());
                    tmpValue = buildFactory.buildCall(curBlock, (Function) getValue("getint"), new ArrayList<>());
                    buildFactory.buildStore(curBlock, input, tmpValue);
                }
                else {
                    List<Value> indexList = new ArrayList<>();
                    visitExp(stmt.getlVal().getExp());
                    indexList.add(tmpValue);
                    tmpValue = getValue(stmt.getlVal().getIdent().getValue());
                    Value addr;
                    Type type = tmpValue.getType();
                    Type targetType = ((PointerType) type).getTargetType();
                    if (targetType instanceof PointerType) {
                        // arr[][3]
                        tmpValue = buildFactory.buildLoad(curBlock, tmpValue);
                    }
                    else {
                        // arr[3][2]
                        indexList.add(0, ConstInt.zero);
                    }
                    addr = buildFactory.buildGEP(curBlock, tmpValue, indexList);
                    Value input = buildFactory.buildCall(curBlock, (Function) getValue("getint"), new ArrayList<>());
                    tmpValue = buildFactory.buildStore(curBlock, addr, input);
                }
                break;

            case LValAssignGetchar:
                if (stmt.getlVal().getExp() == null) {
                    Value input = getValue(stmt.getlVal().getIdent().getValue());
                    tmpValue = buildFactory.buildCall(curBlock, (Function) getValue("getchar"), new ArrayList<>());
                    Value truncValue =  buildFactory.buildTrunc(tmpValue, curBlock, IntegerType.i32, IntegerType.i8);
                    buildFactory.buildStore(curBlock, input, truncValue);
                }
                else {
                    List<Value> indexList = new ArrayList<>();
                    visitExp(stmt.getlVal().getExp());
                    indexList.add(tmpValue);
                    tmpValue = getValue(stmt.getlVal().getIdent().getValue());
                    Value addr;
                    Type type = tmpValue.getType();
                    Type targetType = ((PointerType) type).getTargetType();
                    if (targetType instanceof PointerType) {
                        // arr[][3]
                        tmpValue = buildFactory.buildLoad(curBlock, tmpValue);
                    }
                    else {
                        // arr[3][2]
                        indexList.add(0, ConstInt.zero);
                    }
                    addr = buildFactory.buildGEP(curBlock, tmpValue, indexList);
                    Value input = buildFactory.buildCall(curBlock, (Function) getValue("getchar"), new ArrayList<>());
                    Value truncValue =  buildFactory.buildTrunc(input, curBlock, IntegerType.i32, IntegerType.i8);
                    tmpValue = buildFactory.buildStore(curBlock, addr, truncValue);
                }
                break;

            case Printf:
                // 'printf''('StringConst {','Exp}')'';'
                String stringConst = stmt.getStringConst().getValue().replace("\\n", "\n").replace("\"", "");
                List<Value> args = new ArrayList<>();
                for (AST.Exp exp : stmt.getExpList()) {
                    visitExp(exp);
                    args.add(tmpValue);
                }
                for (int i = 0; i < stringConst.length(); i++) {
                    if (stringConst.charAt(i) == '%') {
                        if (stringConst.charAt(i+1) == 'd'){
                            buildFactory.buildCall(curBlock, (Function) getValue("putint"), new ArrayList<Value>() {{
                                add(args.remove(0));
                            }});
                            i++;
                        }
                        else {
                            buildFactory.buildCall(curBlock, (Function) getValue("putchar"), new ArrayList<Value>() {{
                                add(args.remove(0));
                            }});
                            i++;
                        }
                    }
                    else {
                        if (Config.chToStr) {
                            int j = i;
                            while (j < stringConst.length() && stringConst.charAt(j) != '%') {
                                j++;
                            }
                            String str = stringConst.substring(i, j);
                            if (str.length() == 1) {
                                buildFactory.buildCall(curBlock, (Function) getValue("putchar"), new ArrayList<Value>() {{
                                    add(buildFactory.getConstInt(str.charAt(0), IntegerType.i32));
                                }});
                            }
                            else {
                                Value strAddr = buildFactory.buildGEP(curBlock, getValue(getStringName(str)), new ArrayList<Value>() {{
                                    add(ConstInt.zero);
                                    add(ConstInt.zero);
                                }});
                                buildFactory.buildCall(curBlock, (Function) getValue("putstr"), new ArrayList<Value>() {{
                                    add(strAddr);
                                }});
                                i = j - 1;
                            }
                        }
                        else {
                            int finalI = i;
                            buildFactory.buildCall(curBlock, (Function) getValue("putchar"), new ArrayList<Value>() {{
                                add(buildFactory.getConstInt(stringConst.charAt(finalI), IntegerType.i32));
                            }});
                        }
                    }
                }
                break;

            default:
                throw new RuntimeException("Unknown StmtNode type: " + stmt.getStmtType());

        }
    }

    public void visitForStmt(AST.ForStmt forStmt){
        // ForStmt → LVal '=' Exp
        // ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
        for(int i = 0; i < forStmt.getlVals().size(); i++){
            if (forStmt.getlVals().get(i).getExp() == null){
                // 非数组
                Value input = getValue(forStmt.getlVals().get(i).getIdent().getValue());
                visitExp(forStmt.getExps().get(i));
                if ((((PointerType) input.getType()).getTargetType() instanceof IntegerType) && (((PointerType) input.getType()).getTargetType() == IntegerType.i8) && (tmpValue.getType() == IntegerType.i32)){
                    Value truncValue =  buildFactory.buildTrunc(tmpValue, curBlock, IntegerType.i32, IntegerType.i8);
                    tmpValue = buildFactory.buildStore(curBlock, input, truncValue);
                }
                else if ((((PointerType) input.getType()).getTargetType() instanceof IntegerType) && (((PointerType) input.getType()).getTargetType() == IntegerType.i32) && (tmpValue.getType() == IntegerType.i8)){
                    Value zextValue = buildFactory.buildZext(tmpValue, curBlock, IntegerType.i8, IntegerType.i32);
                    tmpValue = buildFactory.buildStore(curBlock, input, zextValue);
                }
                else tmpValue = buildFactory.buildStore(curBlock, input, tmpValue);
            }
            else {
                // 数组
                List<Value> indexList = new ArrayList<>();
                visitExp(forStmt.getlVals().get(i).getExp());
                indexList.add(tmpValue);

                tmpValue = getValue(forStmt.getlVals().get(i).getIdent().getValue());
                Value addr;
                Type type = tmpValue.getType();
                Type targetType = ((PointerType) type).getTargetType();
                if (targetType instanceof PointerType) {
                    // arr[][3]
                    tmpValue = buildFactory.buildLoad(curBlock, tmpValue);
                } else {
                    // arr[3][2]
                    indexList.add(0, ConstInt.zero);
                }
                addr = buildFactory.buildGEP(curBlock, tmpValue, indexList);
                visitExp(forStmt.getExps().get(i));
                if ((((PointerType) addr.getType()).getTargetType() instanceof IntegerType) && (((PointerType) addr.getType()).getTargetType() == IntegerType.i8) && (tmpValue.getType() == IntegerType.i32)){
                    Value truncValue =  buildFactory.buildTrunc(tmpValue, curBlock, IntegerType.i32, IntegerType.i8);
                    tmpValue = buildFactory.buildStore(curBlock, addr, truncValue);
                }
                else if ((((PointerType) addr.getType()).getTargetType() instanceof IntegerType) && (((PointerType) addr.getType()).getTargetType() == IntegerType.i32) && (tmpValue.getType() == IntegerType.i8)){
                    Value zextValue = buildFactory.buildZext(tmpValue, curBlock, IntegerType.i8, IntegerType.i32);
                    tmpValue = buildFactory.buildStore(curBlock, addr, zextValue);
                }
                else tmpValue = buildFactory.buildStore(curBlock, addr, tmpValue);
            }
        }
    }

    public void visitExp(AST.Exp exp){
        // Exp -> AddExp
        tmpValue = null;
        saveValue = null;
        visitAddExp(exp.getAddExp());
    }

    public void visitCond(AST.Cond cond){
        // Cond → LOrExp
        visitLOrExp(cond.getlOrExp());
    }

    public void visitLVal(AST.LVal lVal){
        // LVal → Ident ['[' Exp ']']
        if (isConst){
            StringBuilder name = new StringBuilder(lVal.getIdent().getValue());
            if (lVal.getExp() != null){
                name.append("0;");
                visitExp(lVal.getExp());
                name.append(buildFactory.getConstInt(saveValue == null ? 0 : saveValue, IntegerType.i32).getValue()).append(";");
            }
            saveValue = getConst(name.toString());
        }
        else {
            if (lVal.getExp() == null){
                // 非数组
                Value addr = getValue(lVal.getIdent().getValue());
                tmpValue = addr;
                Type type = addr.getType();

                if (!(((PointerType) type).getTargetType() instanceof ArrayType)) {
                    tmpValue = buildFactory.buildLoad(curBlock, tmpValue);
                }
                else {
                    List<Value> indexList = new ArrayList<>();
                    indexList.add(ConstInt.zero);
                    indexList.add(ConstInt.zero);
                    tmpValue = buildFactory.buildGEP(curBlock, tmpValue, indexList);
                }
            }
            else {
                // 数组
                // is an array, maybe arr[1][2] or arr[][2]
                List<Value> indexList = new ArrayList<>();
//                System.out.println(tmpType.toString());
                visitExp(lVal.getExp());
//                System.out.println(tmpValue.getName());
                indexList.add(tmpValue);
                tmpValue = getValue(lVal.getIdent().getValue());
                Value addr;
                Type type = tmpValue.getType();
                Type targetType = ((PointerType) type).getTargetType();
                if (targetType instanceof PointerType) {
                    // arr[][3]
                    tmpValue = buildFactory.buildLoad(curBlock, tmpValue);
                }
                else {
                    // arr[1][2]
                    indexList.add(0, ConstInt.zero);
                }
                addr = buildFactory.buildGEP(curBlock, tmpValue, indexList);
                if (((PointerType) addr.getType()).getTargetType() instanceof ArrayType) {
                    List<Value> indexList2 = new ArrayList<>();
                    indexList2.add(ConstInt.zero);
                    indexList2.add(ConstInt.zero);
                    tmpValue = buildFactory.buildGEP(curBlock, addr, indexList2);
                }
                else {
                    tmpValue = buildFactory.buildLoad(curBlock, addr);
                }
            }
        }
    }

    public void visitPrimaryExp(AST.PrimaryExp primaryExp){
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (primaryExp.getExp() != null){
            visitExp(primaryExp.getExp());
        }
        else if (primaryExp.getlVal() != null){
            visitLVal(primaryExp.getlVal());
        }
        else if (primaryExp.getNumber() != null){
            visitNumber(primaryExp.getNumber());
        }
        else {
            visitCharacter(primaryExp.getCharacter());
        }
    }

    public void visitNumber(AST.Number number){
        // Number → IntConst
        if (isConst){
            saveValue = Integer.parseInt(number.getIntConst().getValue());
        }
        else {
            if (tmpType == IntegerType.i32){
                tmpValue = buildFactory.getConstInt(Integer.parseInt(number.getIntConst().getValue()), IntegerType.i32);
            }
            else if (tmpType == IntegerType.i8){
                tmpValue = buildFactory.getConstInt(Integer.parseInt(number.getIntConst().getValue()), IntegerType.i8);
            }
            else tmpValue = buildFactory.getConstInt(Integer.parseInt(number.getIntConst().getValue()), IntegerType.i32);
        }
    }

    public void visitCharacter(AST.Character character){
        // Character → CharConst
        String str = character.getCharConst().getValue();
        if (isConst){
            char ch1 = str.charAt(1);
            if (ch1 != '\\'){
                saveValue = (int) ch1;
            }
            else {
                String s1 = str.substring(1, 3);
                Map<String, Character> escapeMap = new HashMap<>();
                escapeMap.put("\\a", (char) 7);
                escapeMap.put("\\b", (char) 8);
                escapeMap.put("\\n", (char) 10); // 换行符
                escapeMap.put("\\t", (char) 9); // 制表符
                escapeMap.put("\\v", (char) 11);
                escapeMap.put("\\f", (char) 12);
                escapeMap.put("\\0", (char) 0);
                escapeMap.put("\\\\", (char) 92); // 反斜杠
                escapeMap.put("\\\'", (char) 39); // 单引号
                escapeMap.put("\\\"", (char) 34); // 双引号
                if (escapeMap.containsKey(s1)) {
                    saveValue =  (int) escapeMap.get(s1); // 返回 ASCII 值
                }
                else throw new RuntimeException("字符错误");
            }
        }
        else {
            char ch1 = str.charAt(1);
            if (ch1 != '\\'){
                tmpValue = buildFactory.getConstChar((int) ch1);
            }
            else {
                String s1 = str.substring(1, 3);
                Map<String, Character> escapeMap = new HashMap<>();
                escapeMap.put("\\a", (char) 7);
                escapeMap.put("\\b", (char) 8);
                escapeMap.put("\\n", (char) 10); // 换行符
                escapeMap.put("\\t", (char) 9); // 制表符
                escapeMap.put("\\v", (char) 11);
                escapeMap.put("\\f", (char) 12);
                escapeMap.put("\\0", (char) 0);
                escapeMap.put("\\\\", (char) 92); // 反斜杠
                escapeMap.put("\\\'", (char) 39); // 单引号
                escapeMap.put("\\\"", (char) 34); // 双引号
                if (escapeMap.containsKey(s1)) {
                    tmpValue = buildFactory.getConstChar((int) escapeMap.get(s1));
                }
                else throw new RuntimeException("字符错误");
            }
        }
    }

    public void visitUnaryExp(AST.UnaryExp unaryExp){
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp  //  return fun();
        if (unaryExp.getPrimaryExp() != null){
            visitPrimaryExp(unaryExp.getPrimaryExp());
        }
        else if (unaryExp.getIdent() != null){
            tmpValueList = new ArrayList<>();
            if (unaryExp.getFuncRParams() != null){
                visitFuncRParams(unaryExp.getFuncRParams());
            }
            tmpValue = buildFactory.buildCall(curBlock, (Function) getValue(unaryExp.getIdent().getValue()), tmpValueList);
        }
        else {
            // UnaryOp 直接在这里处理
            if (unaryExp.getUnaryOp().getToken().getTokenType() == TokenType.PLUS){
                visitUnaryExp(unaryExp.getUnaryExp());
            }
            else if (unaryExp.getUnaryOp().getToken().getTokenType() == TokenType.MINU){
                visitUnaryExp(unaryExp.getUnaryExp());
                if (isConst){
                    saveValue = -saveValue;
                }
                else {
                    tmpValue = buildFactory.buildBinary(curBlock, Operator.Sub, ConstInt.zero, tmpValue);
                }
            }
            else {
                visitUnaryExp(unaryExp.getUnaryExp());
                tmpValue = buildFactory.buildNot(curBlock, tmpValue);
            }
        }
    }


    public void visitFuncRParams(AST.FuncRParams funcRParams){
        // FuncRParams → Exp { ',' Exp }
        List<Value> args = new ArrayList<>();
        for (AST.Exp exp : funcRParams.getExpList()){
            visitExp(exp);
            args.add(tmpValue);
        }
        tmpValueList = args;
    }

    public void visitMulExp(AST.MulExp mulExp){
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        // MulExp → UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
        if (isConst){
            Integer value = saveValue;
            Operator op = saveOp;
            saveValue = null;
            visitUnaryExp(mulExp.getUnaryExp());
            if (value != null){
                saveValue = calculateConst(op, value, saveValue);
            }
            if (mulExp.getMulExp() != null){
                switch (mulExp.getOpToken().getTokenType()) {
                    case MULT:
                        saveOp = Operator.Mul;
                        break;
                    case DIV:
                        saveOp = Operator.Div;
                        break;
                    case MOD:
                        saveOp = Operator.Mod;
                        break;
                    default:
                        throw new RuntimeException("unknown operator");
                }
                visitMulExp(mulExp.getMulExp());
            }
        }
        else {
            Value value = tmpValue;
            Operator op = tmpOp;
            tmpValue = null;
            visitUnaryExp(mulExp.getUnaryExp());
            if (value != null) {
                tmpValue = buildFactory.buildBinary(curBlock, op, value, tmpValue);
            }
            if (mulExp.getMulExp() != null) {
                if (mulExp.getOpToken().getTokenType() == TokenType.MULT) {
                    tmpOp = Operator.Mul;
                }
                else if (mulExp.getOpToken().getTokenType() == TokenType.DIV) {
                    tmpOp = Operator.Div;
                }
                else {
                    tmpOp = Operator.Mod;
                }
                visitMulExp(mulExp.getMulExp());
            }
        }
    }

    public void visitAddExp(AST.AddExp addExp){
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        if (isConst){
            Integer value = saveValue;
            Operator op = saveOp;
            saveValue = null;
            visitMulExp(addExp.getMulExp());
            if(value != null){
                saveValue = calculateConst(op , value, saveValue);
            }
            if (addExp.getAddExp() != null){
                saveOp = addExp.getOpToken().getTokenType() == TokenType.PLUS ? Operator.Add : Operator.Sub;
                visitAddExp(addExp.getAddExp());
            }
        }
        else {
            Value value = tmpValue;
            Operator op = tmpOp;
            if (Config.addToMul){
                if (tmpValue == null && addExp.getAddExp() != null){
                    // 连加变乘
                    if (Objects.equals(addExp.getMulExp().getStr(), addExp.getAddExp().getMulExp().getStr())){
                        int times =1;
                        AST.MulExp mulExp = addExp.getMulExp();
                        AST.AddExp now = addExp;
                        AST.AddExp next = addExp.getAddExp();
                        while (next != null && next.getMulExp() != null && Objects.equals(mulExp.getStr(), next.getMulExp().getStr()) && now.getOpToken() != null) {
                            times += now.getOpToken().getTokenType() == TokenType.PLUS ? 1 : -1;
                            now = next;
                            next = next.getAddExp();
                        }
                        tmpValue = null;
                        visitMulExp(mulExp);
                        if (times == 2) {
                            tmpValue = buildFactory.buildBinary(curBlock, Operator.Add, tmpValue, tmpValue);
                        }
                        else if (times == 1) {
                            // do nothing
                        }
                        else if (times == 0) {
                            tmpValue = buildFactory.getConstInt(0, IntegerType.i32);
                        }
                        else if (times == -1) {
                            tmpValue = buildFactory.buildBinary(curBlock, Operator.Sub, ConstInt.zero, tmpValue);
                        }
                        else {
                            tmpValue = buildFactory.buildBinary(curBlock, Operator.Mul, tmpValue, buildFactory.getConstInt(times, IntegerType.i32));
                        }
                        tmpOp = now.getOpToken() == null ? Operator.Add : now.getOpToken().getTokenType() == TokenType.PLUS ? Operator.Add : Operator.Sub;
                        if (next != null) {
                            visitAddExp(next);
                        }
                        return;
                    }
                }
            }
            tmpValue = null;
            visitMulExp(addExp.getMulExp());
            if (value != null){
                tmpValue = buildFactory.buildBinary(curBlock, op, value, tmpValue);
            }
            if (addExp.getAddExp() != null){
                tmpOp = addExp.getOpToken().getTokenType() == TokenType.PLUS ? Operator.Add : Operator.Sub;
                visitAddExp(addExp.getAddExp());
            }
        }
    }

    public void visitRelExp(AST.RelExp relExp){
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        Value value = tmpValue;
        Operator op = tmpOp;
        tmpValue = null;
        visitAddExp(relExp.getAddExp());
        if (value != null){
            tmpValue = buildFactory.buildBinary(curBlock, op, value, tmpValue);
        }
        if (relExp.getRelExp() != null){
            switch (relExp.getOpToken().getTokenType()){
                case LSS:
                    tmpOp = Operator.Lt;
                    break;

                case LEQ:
                    tmpOp = Operator.Le;
                    break;

                case GRE:
                    tmpOp = Operator.Gt;
                    break;

                case GEQ:
                    tmpOp = Operator.Ge;
                    break;

                default:
                    throw new RuntimeException("Unknown operator");
            }
            visitRelExp(relExp.getRelExp());
        }
    }

    public void visitEqExp(AST.EqExp eqExp){
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        Value value = tmpValue;
        Operator op = tmpOp;
        tmpValue = null;
        visitRelExp(eqExp.getRelExp());
        if (value != null) {
            tmpValue = buildFactory.buildBinary(curBlock, op, value, tmpValue);
        }
        if (eqExp.getEqExp() != null) {
            tmpOp = eqExp.getOpToken().getTokenType() == TokenType.EQL ? Operator.Eq : Operator.Ne;
            visitEqExp(eqExp.getEqExp());
        }
    }

    public void visitLAndExp(AST.LAndExp lAndExp){
        // LAndExp → EqExp | LAndExp '&&' EqExp
        BasicBlock trueBlock = curTrueBlock;
        BasicBlock falseBlock = curFalseBlock;
        BasicBlock tmpTrueBlock = curTrueBlock;
        BasicBlock thenBlock = null;
        if (lAndExp.getlAndExp() != null) {
            thenBlock = buildFactory.buildBasicBlock(curFunction);
            tmpTrueBlock = thenBlock;
        }
        curTrueBlock = tmpTrueBlock;
        tmpValue = null;
        visitEqExp(lAndExp.getEqExp());
        buildFactory.buildBr(curBlock, tmpValue, curTrueBlock, curFalseBlock);
        curTrueBlock = trueBlock;
        curFalseBlock = falseBlock;
        if (lAndExp.getlAndExp() != null) {
            curBlock = thenBlock;
            visitLAndExp(lAndExp.getlAndExp());
        }
    }

    public void visitLOrExp(AST.LOrExp lOrExp){
        // LOrExp → LAndExp | LOrExp '||' LAndExp
        BasicBlock trueBlock = curTrueBlock;
        BasicBlock falseBlock = curFalseBlock;
        BasicBlock tmpFalseBlock = curFalseBlock;
        BasicBlock thenBlock = null;
        if (lOrExp.getlOrExp() != null) {
            thenBlock = buildFactory.buildBasicBlock(curFunction);
            tmpFalseBlock = thenBlock;
        }
        curFalseBlock = tmpFalseBlock;
        visitLAndExp(lOrExp.getlAndExp());
        curTrueBlock = trueBlock;
        curFalseBlock = falseBlock;
        if (lOrExp.getlOrExp() != null) {
            curBlock = thenBlock;
            visitLOrExp(lOrExp.getlOrExp());
        }
    }

    public void visitConstExp(AST.ConstExp constExp){
        // ConstExp → AddExp
        isConst = true;
        saveValue = null;
        visitAddExp(constExp.getAddExp());
        isConst = false;
    }
}
