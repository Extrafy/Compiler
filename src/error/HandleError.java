package error;

import AST.AST;
import config.Config;
import org.xml.sax.ErrorHandler;
import symbol.*;
import utils.InputOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HandleError {
    private static final HandleError instance = new HandleError();

    private List<Error> errorList = new ArrayList<Error>();

    private SymbolStack symbolStack = SymbolStack.getInstance();

    public static HandleError getInstance() {
        return instance;
    }

    public List<Error> getErrorList() {
        return errorList;
    }

    public void addError(Error newError){
        for (Error error : errorList) {
            if (error.equals(newError)) {
                return;
            }
        }
        errorList.add(newError);
    }

    public void printErrors() {
        errorList.sort(Error::compareTo);
        for (Error error : errorList) {
            InputOutput.writeError(error.toString());
        }
    }

    public void compUnitError(AST.CompUnit compUnit){
        symbolStack.addSymbolTable(false, null, SymbolStack.getCurDeclLineNum(), SymbolTable.getRootSymbolTable());
        SymbolTable.getRootSymbolTable().addSon(symbolStack.getStackTop());
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        for (AST.Decl decl : compUnit.getDeclList()){
            declError(decl);
        }
        for (AST.FuncDef funcDef : compUnit.getFuncDefList()){
            funcDefError(funcDef);
        }
        mainFuncDefError(compUnit.getMainFuncDef());
        if (!errorList.isEmpty()) Config.errorFlag = true;
    }

    public void declError(AST.Decl decl){
        // Decl -> ConstDecl | VarDecl
        if (decl.getConstDecl() != null){
            constDeclError(decl.getConstDecl());
        }
        else {
            varDeclError(decl.getVarDecl());
        }
    }

    public void constDeclError(AST.ConstDecl constDecl){
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
        for (AST.ConstDef constDef : constDecl.getConstDefList()){
            constDefError(constDef, constDecl.getbType());
        }
    }

    public void constDefError(AST.ConstDef constDef, AST.BType bType){
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // b k
        if(symbolStack.isInCurrent(constDef.getIdent().getValue())){
            HandleError.getInstance().addError(new Error(constDef.getIdent().getLine(), ErrorType.b));
            return;
        }
        int size = 0;
        if (constDef.getConstExp() != null){
            size = 1;
            constExpError(constDef.getConstExp());
        }
        SymbolType symbolType = null;
        if (bType.getIntToken()!=null && size == 0){
            symbolType = SymbolType.ConstInt;
        }
        else if (bType.getIntToken()!=null && size == 1){
            symbolType = SymbolType.ConstIntArray;
        }
        else if (bType.getCharToken()!=null && size == 0){
            symbolType = SymbolType.ConstChar;
        }
        else {
            symbolType = SymbolType.ConstCharArray;
        }
        symbolStack.put(constDef.getIdent().getValue(), new VarSymbol(constDef.getIdent().getValue(),symbolType,symbolStack.getStackTop().declLineNum,true, size));
        constInitValError(constDef.getConstInitVal());
    }

    public void constInitValError(AST.ConstInitVal constInitVal){
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if (constInitVal.getlBraceToken() != null){
            for (AST.ConstExp constExp : constInitVal.getConstExpList()){
                constExpError(constExp);
            }
        }
        else if(constInitVal.getStringConst() == null){
            constExpError(constInitVal.getConstExpList().get(0));
        }
    }

    public void varDeclError(AST.VarDecl varDecl){
        // VarDecl → BType VarDef { ',' VarDef } ';' // i
        for (AST.VarDef varDef : varDecl.getVarDefList()){
            varDefError(varDef, varDecl.getbType());
        }
    }

    public void varDefError(AST.VarDef varDef, AST.BType bType){
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // b k
        if (symbolStack.isInCurrent(varDef.getIdent().getValue())){
            HandleError.getInstance().addError(new Error(varDef.getIdent().getLine(), ErrorType.b));
            return;
        }
        int size = 0;
        if (varDef.getConstExp() != null){
            size = 1;
            constExpError(varDef.getConstExp());
        }
        SymbolType symbolType = null;
        if (bType.getIntToken()!=null && size == 0){
            symbolType = SymbolType.Int;
        }
        else if (bType.getIntToken()!=null && size == 1){
            symbolType = SymbolType.IntArray;
        }
        else if (bType.getCharToken()!=null && size == 0){
            symbolType = SymbolType.Char;
        }
        else {
            symbolType = SymbolType.CharArray;
        }
        symbolStack.put(varDef.getIdent().getValue(), new VarSymbol(varDef.getIdent().getValue(), symbolType, symbolStack.getStackTop().declLineNum, false, size));
        if (varDef.getInitVal() != null){
            initValError(varDef.getInitVal());
        }
    }

    public void initValError(AST.InitVal initVal){
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if (initVal.getlBraceToken() != null){
            for (AST.Exp exp : initVal.getExpList()){
                expError(exp);
            }
        }
        else if(initVal.getStringConst() == null) {
            expError(initVal.getExpList().get(0));
        }
    }

    public void funcDefError(AST.FuncDef funcDef){
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g j
        if (symbolStack.isInCurrent(funcDef.getIdent().getValue())){
            HandleError.getInstance().addError(new Error(funcDef.getIdent().getLine(), ErrorType.b));
            return;
        }
        SymbolType symbolType = null;
        FuncSymbol.FuncReturnType funcReturnType = null;
        if (funcDef.getFuncType().getIntToken()!=null){
            symbolType = SymbolType.IntFunc;
            funcReturnType = FuncSymbol.FuncReturnType.INT;
        }
        else if (funcDef.getFuncType().getCharToken()!=null){
            symbolType = SymbolType.CharFunc;
            funcReturnType = FuncSymbol.FuncReturnType.CHAR;
        }
        else {
            symbolType = SymbolType.VoidFunc;
            funcReturnType = FuncSymbol.FuncReturnType.VOID;
        }
        if (funcDef.getFuncFParams() == null){
            symbolStack.put(funcDef.getIdent().getValue(), new FuncSymbol(funcDef.getIdent().getValue(), symbolType, symbolStack.getStackTop().declLineNum, funcReturnType, new ArrayList<>()));
        }
        else {
            List<FuncParam> params = new ArrayList<>();
            for (AST.FuncFParam funcFParam : funcDef.getFuncFParams().getFuncFParamList()){
                int size = 0;
                if (funcFParam.getlBrackToken()!=null){
                    size = 1;
                }
                SymbolType paramSymbolType;
                if (funcFParam.getbType().getIntToken() != null && size == 0){
                    paramSymbolType = SymbolType.Int;
                }
                else if (funcFParam.getbType().getIntToken() != null && size == 1){
                    paramSymbolType = SymbolType.IntArray;
                }
                else if (funcFParam.getbType().getCharToken() != null && size == 0){
                    paramSymbolType = SymbolType.Char;
                }
                else {
                    paramSymbolType = SymbolType.CharArray;
                }
                params.add(new FuncParam(funcFParam.getIdent().getValue(), size, paramSymbolType));  // 没有考虑常量数组
            }
            symbolStack.put(funcDef.getIdent().getValue(), new FuncSymbol(funcDef.getIdent().getValue(),symbolType, symbolStack.getStackTop().declLineNum, funcReturnType, params));
        }
        SymbolTable fatherSymbolTable = symbolStack.getStackTop();
        symbolStack.addCurDeclLineNum(); // 作用域标号+1
        symbolStack.addSymbolTable(true, funcReturnType, SymbolStack.getCurDeclLineNum(), fatherSymbolTable); // 压栈 ?父亲有问题
        fatherSymbolTable.addSon(symbolStack.getStackTop());
        if (funcDef.getFuncFParams() != null){
            funcFParamsError(funcDef.getFuncFParams());
        }
        blockError(funcDef.getBlock());
        symbolStack.removeSymbolTable();
    }

    public void mainFuncDefError(AST.MainFuncDef mainFuncDef){
        // MainFuncDef → 'int' 'main' '(' ')' Block // g j
        SymbolTable fatherSymbolTable = symbolStack.getStackTop();
        symbolStack.addCurDeclLineNum(); // 作用域标号+1
        symbolStack.addSymbolTable(true, FuncSymbol.FuncReturnType.INT, SymbolStack.getCurDeclLineNum(), fatherSymbolTable); // ?父亲有问题
        fatherSymbolTable.addSon(symbolStack.getStackTop());
        blockError(mainFuncDef.getBlock());
        symbolStack.removeSymbolTable();
    }

    public void funcFParamsError(AST.FuncFParams funcFParams){
        // FuncFParams → FuncFParam { ',' FuncFParam }
        for (AST.FuncFParam funcFParam : funcFParams.getFuncFParamList()){
            funcFParamError(funcFParam);
        }
    }

    public void funcFParamError(AST.FuncFParam funcFParam){
        // FuncFParam → BType Ident ['[' ']'] // b k
        if (symbolStack.isInCurrent(funcFParam.getIdent().getValue())){
            HandleError.getInstance().addError(new Error(funcFParam.getIdent().getLine(), ErrorType.b));
        }
        int size = 0;
        if (funcFParam.getlBrackToken() != null){
            size = 1;
        }
        SymbolType symbolType = null;
        if (funcFParam.getbType().getIntToken() != null && size == 0){
            symbolType = SymbolType.Int;
        }
        else if (funcFParam.getbType().getIntToken() != null && size == 1){
            symbolType = SymbolType.IntArray;
        }
        else if (funcFParam.getbType().getCharToken() != null && size == 0){
            symbolType = SymbolType.Char;
        }
        else {
            symbolType = SymbolType.CharArray;
        }
        symbolStack.put(funcFParam.getIdent().getValue(), new VarSymbol(funcFParam.getIdent().getValue(), symbolType, symbolStack.getStackTop().declLineNum, false, size));
    }

    public void blockError(AST.Block block){
        // Block → '{' { BlockItem } '}'
        for (AST.BlockItem blockItem : block.getBlockItemList()){
            blockItemError(blockItem);
        }
        if (symbolStack.isCurrentFunc()){
            if (symbolStack.getCurrentFuncType() != FuncSymbol.FuncReturnType.VOID){
                if (block.getBlockItemList().isEmpty() || block.getBlockItemList().get(block.getBlockItemList().size() - 1).getStmt() == null || block.getBlockItemList().get(block.getBlockItemList().size() - 1).getStmt().getReturnToken() == null){
                    HandleError.getInstance().addError(new Error(block.getrBraceToken().getLine(), ErrorType.g));
                }
            }
        }
    }

    public void blockItemError(AST.BlockItem blockItem){
        // BlockItem → Decl | Stmt
        if (blockItem.getDecl() != null){
            declError(blockItem.getDecl());
        }
        else {
            stmtError(blockItem.getStmt());
        }
    }

    public void stmtError(AST.Stmt stmt){
        /* Stmt → LVal '=' Exp ';' // h i
                | [Exp] ';' // i
                | Block
                | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
                | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // h
                | 'break' ';' | 'continue' ';' // i m
                | 'return' [Exp] ';' // f i
                | LVal '=' 'getint''('')'';' // h i j
                | LVal '=' 'getchar''('')'';' // h i j
                | 'printf''('StringConst {','Exp}')'';' // i j l
        */
        switch (stmt.getStmtType()){
            case Exp:
                // [Exp] ';'
                if (stmt.getExp() != null){
                    expError(stmt.getExp());
                }
                break;

            case Block:
                // Block
                SymbolTable fatherSymbolTable = symbolStack.getStackTop();
                symbolStack.addCurDeclLineNum(); // 作用域标号+1
                symbolStack.addSymbolTable(false, null, SymbolStack.getCurDeclLineNum(), fatherSymbolTable); // ?父亲有问题
                fatherSymbolTable.addSon(symbolStack.getStackTop());
                blockError(stmt.getBlock());
                symbolStack.removeSymbolTable();
                break;

            case If:
                // 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
                condError(stmt.getCond());
                stmtError(stmt.getStmtList().get(0));
                if (stmt.getElseToken() != null){
                    stmtError(stmt.getStmtList().get(1));
                }
                break;

            case For:
                // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // h
                if (stmt.getForStmt1() != null){
                    forStmtError(stmt.getForStmt1());
                }
                if (stmt.getCond() != null){
                    condError(stmt.getCond());
                }
                if (stmt.getForStmt2() != null){
                    forStmtError(stmt.getForStmt2());
                }
                symbolStack.inLoop();
                stmtError(stmt.getStmtList().get(0));
                symbolStack.outLoop();
                break;

            case Break:
                // 'break' ';' // i m

            case Continue:
                // 'continue' ';' // i m
                if (SymbolStack.getLoopCount() == 0){
                    HandleError.getInstance().addError(new Error(stmt.getBreakOrcontinueToken().getLine(), ErrorType.m));
                }
                break;

            case Return:
                // 'return' [Exp] ';' // f i
                if (symbolStack.isInFunc()){
                    if (symbolStack.getReturnType() == FuncSymbol.FuncReturnType.VOID && stmt.getExp() != null){
                        HandleError.getInstance().addError(new Error(stmt.getReturnToken().getLine(), ErrorType.f));
                    }
                    if (stmt.getExp() != null){
                        expError(stmt.getExp());
                    }
                }
                break;

            case LValAssignExp:
                // LVal '=' Exp ';' // h i
                lValError(stmt.getlVal());
                if (symbolStack.get(stmt.getlVal().getIdent().getValue()) instanceof VarSymbol){
                    VarSymbol varSymbol = (VarSymbol) symbolStack.get(stmt.getlVal().getIdent().getValue());
                    if (varSymbol.isConst()){
                        HandleError.getInstance().addError(new Error(stmt.getlVal().getIdent().getLine(), ErrorType.h));
                    }
                }
                expError(stmt.getExp());
                break;
            case LValAssignGetchar:
                // LVal '=' 'getchar''('')'';' // h i j
            case LValAssignGetint:
                // LVal '=' 'getint''('')'';' // h i j
                lValError(stmt.getlVal());
                if (symbolStack.get(stmt.getlVal().getIdent().getValue()) instanceof VarSymbol){
                    VarSymbol varSymbol = (VarSymbol) symbolStack.get(stmt.getlVal().getIdent().getValue());
                    if (varSymbol.isConst()){
                        HandleError.getInstance().addError(new Error(stmt.getlVal().getIdent().getLine(), ErrorType.h));
                    }
                }
                break;

            case Printf:
                // 'printf''('StringConst {','Exp}')'';' // i j l
                int expNum = stmt.getExpList().size();
                int formatStringNum = 0;
                for (int i = 0; i < stmt.getStringConst().getValue().length(); i++){
                    if (stmt.getStringConst().getValue().charAt(i) == '%'){
                        if (stmt.getStringConst().getValue().charAt(i+1) == 'd' || stmt.getStringConst().getValue().charAt(i+1) == 'c'){
                            formatStringNum ++;
                        }
                    }
                }
                if (expNum != formatStringNum){
                    HandleError.getInstance().addError(new Error(stmt.getPrintfToken().getLine(), ErrorType.l));
                }
                for (AST.Exp exp : stmt.getExpList()){
                    expError(exp);
                }
                break;
        }
    }

    public void forStmtError(AST.ForStmt forStmt){
        // ForStmt → LVal '=' Exp // h
        //  ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
        for(int i = 0; i < forStmt.getlVals().size(); i++){
            lValError(forStmt.getlVals().get(i));
            if (symbolStack.get(forStmt.getlVals().get(i).getIdent().getValue()) instanceof VarSymbol){
                VarSymbol varSymbol = (VarSymbol) symbolStack.get(forStmt.getlVals().get(i).getIdent().getValue());
                if (varSymbol.isConst()){
                    HandleError.getInstance().addError(new Error(forStmt.getlVals().get(i).getIdent().getLine(), ErrorType.h));
                }
            }
            expError(forStmt.getExps().get(i));
        }
    }

    public void expError(AST.Exp exp){
        // Exp → AddExp
        addExpError(exp.getAddExp());
    }

    public FuncParam getFuncParamInExp(AST.Exp exp){
        // Exp → AddExp
        return getFuncParamInAddExp(exp.getAddExp());
    }

    public void condError(AST.Cond cond){
        // Cond → LOrExp
        lOrExpError(cond.getlOrExp());
    }

    public void lValError(AST.LVal lVal){
        // LVal → Ident ['[' Exp ']'] // c k
        if (!symbolStack.contains(lVal.getIdent().getValue())){
            HandleError.getInstance().addError(new Error(lVal.getIdent().getLine(), ErrorType.c));
            return;
        }
        if (lVal.getExp() != null){
            expError(lVal.getExp());
        }
    }

    public FuncParam getFuncParamInLVal(AST.LVal lVal){ // ?为什么
        // LVal → Ident ['[' Exp ']'] // c k
        int size = 0;
        if (lVal.getExp() != null) size = 1;
        Symbol symbol = symbolStack.get(lVal.getIdent().getValue());
        return new FuncParam(lVal.getIdent().getValue(), size, symbol.getSymbolType());
    }

    public void primaryExpError(AST.PrimaryExp primaryExp){
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character// j
        if (primaryExp.getExp() != null){
            expError(primaryExp.getExp());
        }
        else if (primaryExp.getlVal() != null){
            lValError(primaryExp.getlVal());
        }
    }

    public FuncParam getFuncParamInPrimaryExp(AST.PrimaryExp primaryExp){
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character// j
        if (primaryExp.getExp() != null){
            return getFuncParamInExp(primaryExp.getExp());
        }
        else if (primaryExp.getlVal() != null){
            return getFuncParamInLVal(primaryExp.getlVal());
        }
        else if (primaryExp.getNumber() != null){
            return new FuncParam(null, 0, SymbolType.Int);
        }
        else {
            return new FuncParam(null, 0, SymbolType.Char);
        }
    }

    public void  unaryExpError(AST.UnaryExp unaryExp){
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // c d e j
        if (unaryExp.getPrimaryExp() != null){
            primaryExpError(unaryExp.getPrimaryExp());
        }
        else if (unaryExp.getUnaryExp() != null){
            unaryExpError(unaryExp.getUnaryExp());
        }
        else {
            if (!symbolStack.contains(unaryExp.getIdent().getValue())){
                HandleError.getInstance().addError(new Error(unaryExp.getIdent().getLine(), ErrorType.c));
                return;
            }
            Symbol symbol = symbolStack.get(unaryExp.getIdent().getValue());
            if (!(symbol instanceof FuncSymbol)){
                HandleError.getInstance().addError(new Error(unaryExp.getIdent().getLine(), ErrorType.e));
                return;
            }
            FuncSymbol funcSymbol = (FuncSymbol) symbol;
            if (unaryExp.getFuncRParams() == null){
                if (!funcSymbol.getFuncParams().isEmpty()){
                    HandleError.getInstance().addError(new Error(unaryExp.getIdent().getLine(),ErrorType.d));
                }
            }
            else {
                if (funcSymbol.getFuncParams().size() != unaryExp.getFuncRParams().getExpList().size()){
                    HandleError.getInstance().addError(new Error(unaryExp.getIdent().getLine(), ErrorType.d));
                }
                List<SymbolType> funcFParamTypes = new ArrayList<>();
                for (FuncParam funcParam : funcSymbol.getFuncParams()){
                    funcFParamTypes.add(funcParam.getSymbolType());
                }
                List<SymbolType> funcRParamTypes = new ArrayList<>();
                if (unaryExp.getFuncRParams() != null){
                    funcRParamsError(unaryExp.getFuncRParams());
                    for (AST.Exp exp : unaryExp.getFuncRParams().getExpList()){
                        FuncParam funcRParam = getFuncParamInExp(exp);
                        if (funcRParam != null){
                            if (funcRParam.getName() == null){ // 数字或单个字符
                                funcRParamTypes.add(funcRParam.getSymbolType());
                            }
                            else {
                                Symbol symbol2 = symbolStack.get(funcRParam.getName());
                                if (symbol2 instanceof VarSymbol){
                                    if (((VarSymbol) symbol2).getDimension() - funcRParam.getDimension() == 0){
                                        if (symbol2.getSymbolType() == SymbolType.IntArray) {
                                            funcRParamTypes.add(SymbolType.Int);
                                        }
                                        else if (symbol2.getSymbolType() == SymbolType.CharArray){
                                            funcRParamTypes.add(SymbolType.Char);
                                        }
                                        else funcRParamTypes.add(funcRParam.getSymbolType());
                                    }
                                    else funcRParamTypes.add(funcRParam.getSymbolType());
                                }
                                else if (symbol2 instanceof  FuncSymbol){
                                    FuncSymbol funcSymbol2 = (FuncSymbol) symbol2;
                                    if (funcSymbol2.getReturnType() == FuncSymbol.FuncReturnType.INT){
                                        funcRParamTypes.add(SymbolType.Int);
                                    }
                                    else if (funcSymbol2.getReturnType() == FuncSymbol.FuncReturnType.CHAR){
                                        funcRParamTypes.add(SymbolType.Char);
                                    }
                                    else funcRParamTypes.add(SymbolType.VoidFunc);
                                }
                                // funcRParamTypes.add(funcRParam.getSymbolType()); // 可优化
                            }
                        }
                    }
                }
                for (int i = 0; i < funcFParamTypes.size() && i < funcRParamTypes.size(); i++) {
                    SymbolType fParam = funcFParamTypes.get(i);
                    SymbolType rParam = funcRParamTypes.get(i);
                    if ((isArray(fParam) && !isArray(rParam)) || (!isArray(fParam) && isArray(rParam))){
                        HandleError.getInstance().addError(new Error(unaryExp.getIdent().getLine(), ErrorType.e));
                        break;
                    }
                    else if ((fParam == SymbolType.CharArray && rParam == SymbolType.IntArray) || (rParam == SymbolType.CharArray && fParam == SymbolType.IntArray)){
                        HandleError.getInstance().addError(new Error(unaryExp.getIdent().getLine(), ErrorType.e));
                        break;
                    }
                }
//                if (!Objects.equals(funcFParamTypes, funcRParamTypes)){
//                    HandleError.getInstance().addError(new Error(unaryExp.getIdent().getLine(), ErrorType.e));
//                }
            }
        }
    }

    public boolean isArray(SymbolType symbolType){
        return symbolType == SymbolType.IntArray || symbolType == SymbolType.CharArray;
    }

    public FuncParam getFuncParamInUnaryExp(AST.UnaryExp unaryExp){
        // UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (unaryExp.getPrimaryExp() != null){
            return getFuncParamInPrimaryExp(unaryExp.getPrimaryExp());
        }
        else if (unaryExp.getIdent() != null){
            Symbol symbol = symbolStack.get(unaryExp.getIdent().getValue());
            if (symbol instanceof FuncSymbol){
                FuncSymbol funcSymbol = (FuncSymbol) symbol;
                if (funcSymbol.getReturnType() == FuncSymbol.FuncReturnType.INT){
                    return new FuncParam(unaryExp.getIdent().getValue(), 0 , SymbolType.Int);
                }
                else if (funcSymbol.getReturnType() == FuncSymbol.FuncReturnType.CHAR){
                    return new FuncParam(unaryExp.getIdent().getValue(), 0, SymbolType.Char);
                }
                else return new FuncParam(unaryExp.getIdent().getValue(), 0, SymbolType.VoidFunc); // 错误
            }
            else return null; // 错误
        }
        else {
            return getFuncParamInUnaryExp(unaryExp.getUnaryExp());
        }
    }

    public void funcRParamsError(AST.FuncRParams funcRParams){
        // FuncRParams → Exp { ',' Exp }
        for (AST.Exp exp : funcRParams.getExpList()){
            expError(exp);
        }
    }

    public void mulExpError(AST.MulExp mulExp){
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        // MulExp -> UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
        unaryExpError(mulExp.getUnaryExp());
        if (mulExp.getMulExp() != null){
            mulExpError(mulExp.getMulExp());
        }
    }

    public FuncParam getFuncParamInMulExp(AST.MulExp mulExp){
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        // MulExp -> UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
        return getFuncParamInUnaryExp(mulExp.getUnaryExp());
    }

    public void addExpError(AST.AddExp addExp){
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        // AddExp -> MulExp | MulExp ('+' | '-') AddExp
        mulExpError(addExp.getMulExp());
        if (addExp.getAddExp() != null){
            addExpError(addExp.getAddExp());
        }
    }

    public FuncParam getFuncParamInAddExp(AST.AddExp addExp){
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        // AddExp -> MulExp | MulExp ('+' | '-') AddExp
        return getFuncParamInMulExp(addExp.getMulExp());
    }

    public void relExpError(AST.RelExp relExp){
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        // RelExp -> AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp
        addExpError(relExp.getAddExp());
        if (relExp.getRelExp() != null){
            relExpError(relExp.getRelExp());
        }
    }

    public void eqExpError(AST.EqExp eqExp){
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        // EqExp -> RelExp | RelExp ('==' | '!=') EqExp
        relExpError(eqExp.getRelExp());
        if (eqExp.getEqExp()!=null){
            eqExpError(eqExp.getEqExp());
        }
    }

    public void lAndExpError(AST.LAndExp lAndExp){
        // LAndExp → EqExp | LAndExp '&&' EqExp // a
        // LAndExp -> EqExp | EqExp '&&' LAndExp
        eqExpError(lAndExp.getEqExp());
        if (lAndExp.getlAndExp() != null){
            lAndExpError(lAndExp.getlAndExp());
        }
    }

    public void lOrExpError(AST.LOrExp lOrExp){
        // LOrExp → LAndExp | LOrExp '||' LAndExp // a
        // LOrExp -> LAndExp | LAndExp '||' LOrExp
        lAndExpError(lOrExp.getlAndExp());
        if (lOrExp.getlOrExp() != null){
            lOrExpError(lOrExp.getlOrExp());
        }
    }

    public void constExpError(AST.ConstExp constExp){
        // ConstExp → AddExp 注：使用的 Ident 必须是常量
        // ConstExp -> AddExp
        addExpError(constExp.getAddExp());
    }

}
