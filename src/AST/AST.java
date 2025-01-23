package AST;


import token.Token;
import utils.InputOutput;

import java.util.ArrayList;
import java.util.List;

public class AST {
    public CompUnit compUnit;
    public AST(CompUnit compUnit){
        this.compUnit = compUnit;
    }

    public void print(){
        compUnit.print();
    }

    public static class CompUnit{
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        private List<Decl> declList = new ArrayList<Decl>();
        private List<FuncDef> funcDefList = new ArrayList<FuncDef>();
        private MainFuncDef mainFuncDef;
        private String type = "<CompUnit>";

        public CompUnit(List<Decl> declList, List<FuncDef> funcDefList, MainFuncDef mainFuncDef) {
            this.declList = declList;
            this.funcDefList = funcDefList;
            this.mainFuncDef = mainFuncDef;
        }

        public List<Decl> getDeclList() {
            return declList;
        }

        public List<FuncDef> getFuncDefList() {
            return funcDefList;
        }

        public MainFuncDef getMainFuncDef() {
            return mainFuncDef;
        }

        public String getType() {
            return type;
        }

        public void print() {
            for (Decl decl : declList){
                decl.print();
            }
            for (FuncDef funcDef : funcDefList){
                funcDef.print();
            }
            mainFuncDef.print();
            InputOutput.write(getType());
        }
    }

    public static class Decl{
        // Decl → ConstDecl | VarDecl
        private ConstDecl constDecl;
        private VarDecl varDecl;
        private String type = "<Decl>";

        public Decl(ConstDecl constDecl, VarDecl varDecl) {
            this.constDecl = constDecl;
            this.varDecl = varDecl;
        }

        public ConstDecl getConstDecl() {
            return constDecl;
        }

        public VarDecl getVarDecl() {
            return varDecl;
        }

        public String getType() {
            return type;
        }

        public void print(){
            if (constDecl != null){
                constDecl.print();
            }
            else {
                varDecl.print();
            }
        }
    }

    public static class FuncDef{
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        private FuncType funcType;
        private Token ident;
        private Token lBraceToken;
        private FuncFParams funcFParams;
        private Token rBraceToken;
        private Block block;
        private String type = "<FuncDef>";

        public FuncDef(FuncType funcType, Token ident, Token lBraceToken, FuncFParams funcFParams, Token rBraceToken, Block block) {
            this.funcType = funcType;
            this.ident = ident;
            this.lBraceToken = lBraceToken;
            this.funcFParams = funcFParams;
            this.rBraceToken = rBraceToken;
            this.block = block;
        }

        public FuncType getFuncType() {
            return funcType;
        }

        public Token getIdent() {
            return ident;
        }

        public Token getlBraceToken() {
            return lBraceToken;
        }

        public FuncFParams getFuncFParams() {
            return funcFParams;
        }

        public Token getrBraceToken() {
            return rBraceToken;
        }

        public Block getBlock() {
            return block;
        }

        public String getType() {
            return type;
        }

        public void print(){
            funcType.print();
            InputOutput.write(ident.toString());
            InputOutput.write(lBraceToken.toString());
            if(funcFParams != null){
                funcFParams.print();
            }
            InputOutput.write(rBraceToken.toString());
            block.print();
            InputOutput.write(getType());
        }
    }
    public static class MainFuncDef{
        // MainFuncDef → 'int' 'main' '(' ')' Block
        private Token intToken;
        private Token mainToken;
        private Token lParentToken;
        private Token rParentToken;
        private Block block;

        private String type = "<MainFuncDef>";

        public MainFuncDef(Token intToken, Token mainToken, Token lParentToken, Token rParentToken, Block block) {
            this.intToken = intToken;
            this.mainToken = mainToken;
            this.lParentToken = lParentToken;
            this.rParentToken = rParentToken;
            this.block = block;
        }

        public Token getIntToken() {
            return intToken;
        }

        public Token getMainToken() {
            return mainToken;
        }

        public Token getlParentToken() {
            return lParentToken;
        }

        public Token getrParentToken() {
            return rParentToken;
        }

        public Block getBlock() {
            return block;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(intToken.toString());
            InputOutput.write(mainToken.toString());
            InputOutput.write(lParentToken.toString());
            InputOutput.write(rParentToken.toString());
            block.print();
            InputOutput.write(type);
        }
    }

    public static class ConstDecl{
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        private Token constToken;
        private BType bType;
        private List<ConstDef> constDefList;
        private List<Token> commaTokens;
        private Token semicnToken;

        private String type = "<ConstDecl>";

        public ConstDecl(Token constToken, BType bType, List<ConstDef> constDefList, List<Token> commaTokens, Token semicnToken) {
            this.constToken = constToken;
            this.bType = bType;
            this.constDefList = constDefList;
            this.commaTokens = commaTokens;
            this.semicnToken = semicnToken;
        }

        public Token getConstToken() {
            return constToken;
        }

        public BType getbType() {
            return bType;
        }

        public List<ConstDef> getConstDefList() {
            return constDefList;
        }

        public List<Token> getCommaTokens() {
            return commaTokens;
        }

        public Token getSemicnToken() {
            return semicnToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(constToken.toString());
            bType.print();
            constDefList.get(0).print();
            for(int i = 1; i < constDefList.size(); i++){
                InputOutput.write(commaTokens.get(i-1).toString());
                constDefList.get(i).print();
            }
            InputOutput.write(semicnToken.toString());
            InputOutput.write(getType());
        }

    }

    public static class VarDecl{
        // VarDecl → BType VarDef { ',' VarDef } ';'
        private BType bType;
        private List<VarDef> varDefList;
        private List<Token> commaTokens;
        private Token semicnToken;

        private String type = "<VarDecl>";

        public VarDecl(BType bType, List<VarDef> varDefList, List<Token> commaTokens, Token semicnToken) {
            this.bType = bType;
            this.varDefList = varDefList;
            this.commaTokens = commaTokens;
            this.semicnToken = semicnToken;
        }

        public BType getbType() {
            return bType;
        }

        public List<VarDef> getVarDefList() {
            return varDefList;
        }

        public List<Token> getCommaTokens() {
            return commaTokens;
        }

        public Token getSemicnToken() {
            return semicnToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            bType.print();
            varDefList.get(0).print();
            for (int i = 1; i < varDefList.size(); i++){
                InputOutput.write(commaTokens.get(i-1).toString());
                varDefList.get(i).print();
            }
            InputOutput.write(semicnToken.toString());
            InputOutput.write(getType());
        }

    }

    public static class BType{
        // BType → 'int' | 'char'
        private Token intToken;
        private Token charToken;
        private String type = "<BType>";

        public BType(Token intToken, Token charToken) {
            this.intToken = intToken;
            this.charToken = charToken;
        }

        public Token getIntToken() {
            return intToken;
        }

        public Token getCharToken() {
            return charToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            if (intToken != null){
                InputOutput.write(intToken.toString());
            }
            else {
                InputOutput.write(charToken.toString());
            }
        }

    }

    public static class ConstDef{
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        private Token ident;
        private Token lBrackToken;
        private ConstExp constExp;
        private Token rBrackToken;

        private Token assignToken;

        private ConstInitVal constInitVal;

        private String type = "<ConstDef>";

        public ConstDef(Token ident, Token lBrackToken, ConstExp constExp, Token rBrackToken, Token assignToken, ConstInitVal constInitVal) {
            this.ident = ident;
            this.lBrackToken = lBrackToken;
            this.constExp = constExp;
            this.rBrackToken = rBrackToken;
            this.assignToken = assignToken;
            this.constInitVal = constInitVal;
        }

        public Token getIdent() {
            return ident;
        }

        public Token getlBrackToken() {
            return lBrackToken;
        }

        public ConstExp getConstExp() {
            return constExp;
        }

        public Token getrBrackToken() {
            return rBrackToken;
        }

        public Token getAssignToken() {
            return assignToken;
        }

        public ConstInitVal getConstInitVal() {
            return constInitVal;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(ident.toString());
            if(constExp != null){
                InputOutput.write(lBrackToken.toString());
                constExp.print();
                InputOutput.write(rBrackToken.toString());
            }
            InputOutput.write(assignToken.toString());
            constInitVal.print();
            InputOutput.write(getType());
        }
    }

    public static class ConstExp{
        // ConstExp → AddExp
        private AddExp addExp;
        private String type = "<ConstExp>";

        public ConstExp(AddExp addExp) {
            this.addExp = addExp;
        }

        public AddExp getAddExp() {
            return addExp;
        }

        public String getType() {
            return type;
        }

        public void print(){
            addExp.print();
            InputOutput.write(getType());
        }

    }

    public static class ConstInitVal{
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        private Token lBraceToken;
        private List<ConstExp> constExpList;
        private List<Token> commaTokens;
        private Token rBraceToken;
        private Token stringConst;

        private String type = "<ConstInitVal>";

        public ConstInitVal(Token lBraceToken, List<ConstExp> constExpList, List<Token> commaTokens, Token rBraceToken, Token stringConst) {
            this.lBraceToken = lBraceToken;
            this.constExpList = constExpList;
            this.commaTokens = commaTokens;
            this.rBraceToken = rBraceToken;
            this.stringConst = stringConst;
        }

        public Token getlBraceToken() {
            return lBraceToken;
        }

        public List<ConstExp> getConstExpList() {
            return constExpList;
        }

        public List<Token> getCommaTokens() {
            return commaTokens;
        }

        public Token getrBraceToken() {
            return rBraceToken;
        }

        public Token getStringConst() {
            return stringConst;
        }

        public String getType() {
            return type;
        }

        public void print(){
            if (lBraceToken == null && stringConst == null){
                constExpList.get(0).print();
            }
            else if (stringConst != null){
                InputOutput.write(stringConst.toString());
            }
            else {
                InputOutput.write(lBraceToken.toString());
                if(!constExpList.isEmpty()){
                    constExpList.get(0).print();
                    for (int i = 1; i < constExpList.size(); i++){
                        InputOutput.write(commaTokens.get(i-1).toString());
                        constExpList.get(i).print();
                    }
                }
                InputOutput.write(rBraceToken.toString());
            }
            InputOutput.write(getType());
        }

    }

    public static class VarDef{
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        private Token ident;
        private Token lBrackToken;
        private ConstExp constExp;
        private Token rBrackToken;
        private Token assignToken;
        private InitVal initVal;
        private String type = "<VarDef>";

        public VarDef(Token ident, Token lBrackToken, ConstExp constExp, Token rBrackToken, Token assignToken, InitVal initVal) {
            this.ident = ident;
            this.lBrackToken = lBrackToken;
            this.constExp = constExp;
            this.rBrackToken = rBrackToken;
            this.assignToken = assignToken;
            this.initVal = initVal;
        }

        public Token getIdent() {
            return ident;
        }

        public Token getlBrackToken() {
            return lBrackToken;
        }

        public ConstExp getConstExp() {
            return constExp;
        }

        public Token getrBrackToken() {
            return rBrackToken;
        }

        public Token getAssignToken() {
            return assignToken;
        }

        public InitVal getInitVal() {
            return initVal;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(ident.toString());
            if (constExp != null){
                InputOutput.write(lBrackToken.toString());
                constExp.print();
                InputOutput.write(rBrackToken.toString());
            }
            if(initVal != null){
                InputOutput.write(assignToken.toString());
                initVal.print();
            }
            InputOutput.write(getType());
        }
    }

    public static class InitVal{
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        private List<Exp> expList;
        private Token lBraceToken;
        private List<Token> commaTokens;
        private Token rBraceToken;
        private Token stringConst;
        private String type;

        public InitVal(List<Exp> expList, Token lBraceToken, List<Token> commaTokens, Token rBraceToken, Token stringConst) {
            this.expList = expList;
            this.lBraceToken = lBraceToken;
            this.commaTokens = commaTokens;
            this.rBraceToken = rBraceToken;
            this.stringConst = stringConst;
        }

        public List<Exp> getExpList() {
            return expList;
        }

        public Token getlBraceToken() {
            return lBraceToken;
        }

        public List<Token> getCommaTokens() {
            return commaTokens;
        }

        public Token getrBraceToken() {
            return rBraceToken;
        }

        public Token getStringConst() {
            return stringConst;
        }

        public String getType() {
            return type;
        }

        public void print(){
            if(lBraceToken == null && stringConst == null){
                expList.get(0).print();
            }
            else if (lBraceToken != null){
                InputOutput.write(lBraceToken.toString());
                if(!expList.isEmpty()){
                    for (int i = 0; i < expList.size(); i++){
                        expList.get(i).print();
                        if(i != expList.size() - 1){
                            InputOutput.write(commaTokens.get(i).toString());
                        }
                    }
                }
                InputOutput.write(rBraceToken.toString());
            }
            else {
                InputOutput.write(stringConst.toString());
            }
            InputOutput.write(getType());
        }

    }

    public static class Exp{
        // Exp → AddExp
        private AddExp addExp;

        private String type = "<Exp>";

        public Exp(AddExp addExp) {
            this.addExp = addExp;
        }

        public AddExp getAddExp() {
            return addExp;
        }

        public String getType() {
            return type;
        }

        public void print(){
            addExp.print();
            InputOutput.write(getType());
        }

    }


    public static class FuncType{
        // FuncType → 'void' | 'int' | 'char'
        private Token voidToken;
        private Token intToken;
        private Token charToken;

        private String type = "<FuncType>";

        public FuncType(Token voidToken, Token intToken, Token charToken) {
            this.voidToken = voidToken;
            this.intToken = intToken;
            this.charToken = charToken;
        }

        public Token getVoidToken() {
            return voidToken;
        }

        public Token getIntToken() {
            return intToken;
        }

        public Token getCharToken() {
            return charToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            if(voidToken == null && intToken == null){
                InputOutput.write(charToken.toString());
            }
            else if(voidToken == null){
                InputOutput.write(intToken.toString());
            }
            else {
                InputOutput.write(voidToken.toString());
            }
            InputOutput.write(getType());
        }
    }

    public static class FuncFParams{
        // FuncFParams → FuncFParam { ',' FuncFParam }
        private List<FuncFParam> funcFParamList;
        private List<Token> commaTokens;

        private String type = "<FuncFParams>";

        public FuncFParams(List<FuncFParam> funcFParamList, List<Token> commaTokens) {
            this.funcFParamList = funcFParamList;
            this.commaTokens = commaTokens;
        }

        public List<FuncFParam> getFuncFParamList() {
            return funcFParamList;
        }

        public List<Token> getCommaTokens() {
            return commaTokens;
        }

        public String getType() {
            return type;
        }

        public void print(){
            funcFParamList.get(0).print();
            for (int i = 1; i < funcFParamList.size(); i++){
                InputOutput.write(commaTokens.get(i-1).toString());
                funcFParamList.get(i).print();
            }
            InputOutput.write(getType());
        }

    }

    public static class Block{
        // Block → '{' { BlockItem } '}'
        private Token lBraceToken;
        private List<BlockItem> blockItemList;
        private Token rBraceToken;
        private String type = "<Block>";

        public Block(Token lBraceToken, List<BlockItem> blockItemList, Token rBraceToken) {
            this.lBraceToken = lBraceToken;
            this.blockItemList = blockItemList;
            this.rBraceToken = rBraceToken;
        }

        public Token getlBraceToken() {
            return lBraceToken;
        }

        public List<BlockItem> getBlockItemList() {
            return blockItemList;
        }

        public Token getrBraceToken() {
            return rBraceToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(lBraceToken.toString());
            if(!blockItemList.isEmpty()){
                for (BlockItem blockItem : blockItemList) {
                    blockItem.print();
                }
            }
            InputOutput.write(rBraceToken.toString());
            InputOutput.write(getType());
        }

    }

    public static class FuncFParam{
        // FuncFParam → BType Ident ['[' ']']
        private BType bType;
        private Token ident;
        private Token lBrackToken;
        private Token rBrackToken;
        private String type = "<FuncFParam>";

        public FuncFParam(BType bType, Token ident, Token lBrackToken, Token rBrackToken) {
            this.bType = bType;
            this.ident = ident;
            this.lBrackToken = lBrackToken;
            this.rBrackToken = rBrackToken;
        }

        public BType getbType() {
            return bType;
        }

        public Token getIdent() {
            return ident;
        }

        public Token getlBrackToken() {
            return lBrackToken;
        }

        public Token getrBrackToken() {
            return rBrackToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            bType.print();
            InputOutput.write(ident.toString());
            if(lBrackToken != null){
                InputOutput.write(lBrackToken.toString());
                InputOutput.write(rBrackToken.toString());
            }
            InputOutput.write(getType());
        }

    }

    public static class BlockItem{
        // BlockItem → Decl | Stmt
        private Decl decl;
        private Stmt stmt;
        private String type = "<BlockItem>";

        public BlockItem(Decl decl, Stmt stmt) {
            this.decl = decl;
            this.stmt = stmt;
        }

        public Decl getDecl() {
            return decl;
        }

        public Stmt getStmt() {
            return stmt;
        }

        public String getType() {
            return type;
        }

        public void print(){
            if (decl != null){
                decl.print();
            }
            else {
                stmt.print();
            }
        }

    }

    public static class Stmt{
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
        public enum StmtType{
            LValAssignExp, Exp, Block, If, For, Break, Continue, Return, LValAssignGetint, LValAssignGetchar, Printf
        }

        private StmtType stmtType;
        private LVal lVal;
        private Token assignToken;
        private Exp exp;
        private Token semicnToken;

        private Token semicnToken2;
        private Block block;
        private Token ifToken;
        private Token lParentToken;
        private Cond cond;
        private Token rParentToken;
        private List<Stmt> stmtList;
        private Token elseToken;
        private Token forToken;
        private List<ForStmt> forStmtList;
        private Token breakOrcontinueToken;
        private Token returnToken;
        private Token getintOrGetcharToken;
        private Token getcharToken;
        private Token printfToken;
        private Token stringConst;
        private List<Token> commaTokens;
        private List<Exp> expList;

        private String type = "<Stmt>";

        public Stmt(StmtType stmtType, LVal lVal, Token assignToken, Exp exp, Token semicnToken) {
            // LVal '=' Exp ';'
            this.stmtType = stmtType;
            this.lVal = lVal;
            this.assignToken = assignToken;
            this.exp = exp;
            this.semicnToken = semicnToken;
        }

        public Stmt(StmtType stmtType, Exp exp, Token semicnToken) {
            // [Exp] ';'
            this.stmtType = stmtType;
            this.exp = exp;
            this.semicnToken = semicnToken;
        }

        public Stmt(StmtType stmtType, Block block) {
            // Block
            this.stmtType = stmtType;
            this.block = block;
        }

        public Stmt(StmtType stmtType, Token ifToken, Token lParentToken, Cond cond, Token rParentToken, List<Stmt> stmtList, Token elseToken) {
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            this.stmtType = stmtType;
            this.ifToken = ifToken;
            this.lParentToken = lParentToken;
            this.cond = cond;
            this.rParentToken = rParentToken;
            this.stmtList = stmtList;
            this.elseToken = elseToken;
        }

        public Stmt(StmtType stmtType, Token semicnToken, Token semicnToken2, Token lParentToken, Cond cond, Token rParentToken, List<Stmt> stmtList, Token forToken, List<ForStmt> forStmtList) {
            // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            this.stmtType = stmtType;
            this.semicnToken = semicnToken;
            this.semicnToken2 = semicnToken2;
            this.lParentToken = lParentToken;
            this.cond = cond;
            this.rParentToken = rParentToken;
            this.stmtList = stmtList;
            this.forToken = forToken;
            this.forStmtList = forStmtList;
        }

        public Stmt(StmtType stmtType, Token semicnToken, Token breakOrcontinueToken) {
            // 'break' ';' | 'continue' ';'
            this.stmtType = stmtType;
            this.semicnToken = semicnToken;
            this.breakOrcontinueToken = breakOrcontinueToken;
        }

        public Stmt(StmtType stmtType, Exp exp, Token semicnToken, Token returnToken) {
            // 'return' [Exp] ';'
            this.stmtType = stmtType;
            this.exp = exp;
            this.semicnToken = semicnToken;
            this.returnToken = returnToken;
        }

        public Stmt(StmtType stmtType, LVal lVal, Token assignToken, Token semicnToken, Token lParentToken, Token rParentToken, Token getintOrGetcharToken) {
            // LVal '=' 'getint''('')'';'
            // LVal '=' 'getchar''('')'';'
            this.stmtType = stmtType;
            this.lVal = lVal;
            this.assignToken = assignToken;
            this.semicnToken = semicnToken;
            this.lParentToken = lParentToken;
            this.rParentToken = rParentToken;
            this.getintOrGetcharToken = getintOrGetcharToken;
        }

        public Stmt(StmtType stmtType, Token semicnToken, Token lParentToken, Token rParentToken, Token printfToken, Token stringConst, List<Token> commaTokens, List<Exp> expList) {
            // 'printf''('StringConst {','Exp}')'';'
            this.stmtType = stmtType;
            this.semicnToken = semicnToken;
            this.lParentToken = lParentToken;
            this.rParentToken = rParentToken;
            this.printfToken = printfToken;
            this.stringConst = stringConst;
            this.commaTokens = commaTokens;
            this.expList = expList;
        }

        public StmtType getStmtType() {
            return stmtType;
        }

        public LVal getlVal() {
            return lVal;
        }

        public Token getAssignToken() {
            return assignToken;
        }

        public Exp getExp() {
            return exp;
        }

        public Token getSemicnToken() {
            return semicnToken;
        }

        public Token getSemicnToken2() {
            return semicnToken2;
        }

        public Block getBlock() {
            return block;
        }

        public Token getIfToken() {
            return ifToken;
        }

        public Token getlParentToken() {
            return lParentToken;
        }

        public Cond getCond() {
            return cond;
        }

        public Token getrParentToken() {
            return rParentToken;
        }

        public List<Stmt> getStmtList() {
            return stmtList;
        }

        public Token getElseToken() {
            return elseToken;
        }

        public Token getForToken() {
            return forToken;
        }

        public List<ForStmt> getForStmtList() {
            return forStmtList;
        }

        public Token getBreakOrcontinueToken() {
            return breakOrcontinueToken;
        }

        public Token getReturnToken() {
            return returnToken;
        }

        public Token getGetintOrGetcharToken() {
            return getintOrGetcharToken;
        }

        public Token getGetcharToken() {
            return getcharToken;
        }

        public Token getPrintfToken() {
            return printfToken;
        }

        public Token getStringConst() {
            return stringConst;
        }

        public List<Token> getCommaTokens() {
            return commaTokens;
        }

        public List<Exp> getExpList() {
            return expList;
        }

        public String getType() {
            return type;
        }

        public void print(){
            switch (stmtType){
                case LValAssignExp :
                    lVal.print();
                    InputOutput.write(assignToken.toString());
                    exp.print();
                    InputOutput.write(semicnToken.toString());
                    break;

                case Exp:
                    if(exp != null){
                        exp.print();
                    }
                    InputOutput.write(semicnToken.toString());

                case Block:
                    block.print();
                    break;

                case If:
                    InputOutput.write(ifToken.toString());
                    InputOutput.write(lParentToken.toString());
                    cond.print();
                    InputOutput.write(rParentToken.toString());
                    stmtList.get(0).print();
                    if (elseToken != null){
                        InputOutput.write(elseToken.toString());
                        stmtList.get(1).print();
                    }
                    break;

                case For:
                    InputOutput.write(forToken.toString());
                    InputOutput.write(lParentToken.toString());
                    if (cond != null && forStmtList.size() == 2){
                        forStmtList.get(0).print();
                        InputOutput.write(semicnToken.toString());
                        cond.print();
                        InputOutput.write(semicnToken2.toString());
                        forStmtList.get(1).print();
                    }
                    else if (cond == null && forStmtList.size() == 2){
                        forStmtList.get(0).print();
                        InputOutput.write(semicnToken.toString());
                        InputOutput.write(semicnToken2.toString());
                        forStmtList.get(1).print();
                    }
                    else if(cond != null && forStmtList.get(0)!=null && forStmtList.get(1) == null){
                        forStmtList.get(0).print();
                        InputOutput.write(semicnToken.toString());
                        cond.print();
                        InputOutput.write(semicnToken2.toString());
                    }
                    else if(cond != null && forStmtList.get(1)!=null && forStmtList.get(0) == null){
                        InputOutput.write(semicnToken.toString());
                        cond.print();
                        InputOutput.write(semicnToken2.toString());
                        forStmtList.get(1).print();
                    }
                    else if (cond == null && forStmtList.get(0)==null && forStmtList.get(1) != null){
                        InputOutput.write(semicnToken.toString());
                        InputOutput.write(semicnToken2.toString());
                        forStmtList.get(1).print();
                    }
                    else if (cond == null && forStmtList.get(1)==null && forStmtList.get(0) != null){
                        forStmtList.get(0).print();
                        InputOutput.write(semicnToken.toString());
                        InputOutput.write(semicnToken2.toString());
                    }
                    else if (cond != null && forStmtList.isEmpty()){
                        InputOutput.write(semicnToken.toString());
                        cond.print();
                        InputOutput.write(semicnToken2.toString());
                    }
                    else {
                        InputOutput.write(semicnToken.toString());
                        InputOutput.write(semicnToken2.toString());
                    }
                    InputOutput.write(rParentToken.toString());
                    stmtList.get(0).print();

                case Break:

                case Continue:
                    InputOutput.write(breakOrcontinueToken.toString());
                    InputOutput.write(semicnToken.toString());
                    break;

                case Return:
                    InputOutput.write(returnToken.toString());
                    if (exp != null){
                        exp.print();
                    }
                    InputOutput.write(semicnToken.toString());
                    break;

                case LValAssignGetint:

                case LValAssignGetchar:
                    lVal.print();
                    InputOutput.write(assignToken.toString());
                    InputOutput.write(getintOrGetcharToken.toString());
                    InputOutput.write(lParentToken.toString());
                    InputOutput.write(rParentToken.toString());
                    InputOutput.write(semicnToken.toString());
                    break;

                case Printf:
                    InputOutput.write(printfToken.toString());
                    InputOutput.write(lParentToken.toString());
                    InputOutput.write(stringConst.toString());
                    for (int i = 0 ; i < commaTokens.size(); i++){
                        InputOutput.write(commaTokens.get(i).toString());
                        expList.get(i).print();
                    }
                    InputOutput.write(rParentToken.toString());
                    InputOutput.write(semicnToken.toString());
                    break;
            }
            InputOutput.write(getType());
        }

    }

    public static class LVal{
        // LVal → Ident ['[' Exp ']']
        private Token ident;
        private Token lBrackToken;
        private Exp exp;
        private Token rBrackToken;
        private String type = "<LVal>";

        public LVal(Token ident, Token lBrackToken, Exp exp, Token rBrackToken) {
            this.ident = ident;
            this.lBrackToken = lBrackToken;
            this.exp = exp;
            this.rBrackToken = rBrackToken;
        }

        public Token getIdent() {
            return ident;
        }

        public Token getlBrackToken() {
            return lBrackToken;
        }

        public Exp getExp() {
            return exp;
        }

        public Token getrBrackToken() {
            return rBrackToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(ident.toString());
            if(exp != null){
                InputOutput.write(lBrackToken.toString());
                exp.print();
                InputOutput.write(rBrackToken.toString());
            }
            InputOutput.write(getType());
        }

    }

    public static class Cond{
        // Cond → LOrExp
        private LOrExp lOrExp;
        private String type = "<Cond>";

        public Cond(LOrExp lOrExp) {
            this.lOrExp = lOrExp;
        }

        public LOrExp getlOrExp() {
            return lOrExp;
        }

        public String getType() {
            return type;
        }

        public void print(){
            lOrExp.print();
            InputOutput.write(getType());
        }

    }

    public static class ForStmt{
        // ForStmt → LVal '=' Exp
        private LVal lVal;
        private Token assignToken;
        private Exp exp;
        private String type = "<ForStmt>";

        public ForStmt(LVal lVal, Token assignToken, Exp exp) {
            this.lVal = lVal;
            this.assignToken = assignToken;
            this.exp = exp;
        }

        public LVal getlVal() {
            return lVal;
        }

        public Token getAssignToken() {
            return assignToken;
        }

        public Exp getExp() {
            return exp;
        }

        public String getType() {
            return type;
        }

        public void print(){
            lVal.print();
            InputOutput.write(assignToken.toString());
            exp.print();
            InputOutput.write(getType());
        }

    }

    public static class AddExp{
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        private MulExp mulExp;
        private AddExp addExp;
        private Token opToken;
        private String type = "<AddExp>";

        public AddExp(MulExp mulExp, AddExp addExp, Token opToken) {
            this.mulExp = mulExp;
            this.addExp = addExp;
            this.opToken = opToken;
        }

        public MulExp getMulExp() {
            return mulExp;
        }

        public AddExp getAddExp() {
            return addExp;
        }

        public Token getOpToken() {
            return opToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            mulExp.print();
            InputOutput.write(getType());
            if(opToken != null){
                InputOutput.write(opToken.toString());
                addExp.print();
            }
        }

    }

    public static class LOrExp{
        // LOrExp → LAndExp | LOrExp '||' LAndExp
        private LAndExp lAndExp;
        private LOrExp lOrExp;
        private Token opToken;
        private String type = "<LOrExp>";

        public LOrExp(LAndExp lAndExp, LOrExp lOrExp, Token opToken) {
            this.lAndExp = lAndExp;
            this.lOrExp = lOrExp;
            this.opToken = opToken;
        }

        public LAndExp getlAndExp() {
            return lAndExp;
        }

        public LOrExp getlOrExp() {
            return lOrExp;
        }

        public Token getOpToken() {
            return opToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            lAndExp.print();
            InputOutput.write(getType());
            if(opToken != null){
                InputOutput.write(opToken.toString());
                if (lOrExp != null){
                    lOrExp.print();
                }
            }
        }

    }

    public static class PrimaryExp{
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        private Token lParentToken;
        private Exp exp;
        private Token rParentToken;
        private LVal lVal;
        private Number number;
        private Character character;

        private String type = "<PrimaryExp>";

        public PrimaryExp(Token lParentToken, Exp exp, Token rParentToken, LVal lVal, Number number, Character character) {
            this.lParentToken = lParentToken;
            this.exp = exp;
            this.rParentToken = rParentToken;
            this.lVal = lVal;
            this.number = number;
            this.character = character;
        }

        public Token getlParentToken() {
            return lParentToken;
        }

        public Exp getExp() {
            return exp;
        }

        public Token getrParentToken() {
            return rParentToken;
        }

        public LVal getlVal() {
            return lVal;
        }

        public Number getNumber() {
            return number;
        }

        public Character getCharacter() {
            return character;
        }

        public String getType() {
            return type;
        }

        public void print(){
            if (exp != null){
                InputOutput.write(lParentToken.toString());
                exp.print();
                InputOutput.write(rParentToken.toString());
            }
            else if(lVal != null){
                lVal.print();
            }
            else if(number != null){
                number.print();
            }
            else {
                character.print();
            }
            InputOutput.write(getType());
        }

    }

    public static class Number{
        // Number → IntConst
        private Token intConst;
        private String type = "<Number>";

        public Number(Token intConst) {
            this.intConst = intConst;
        }

        public Token getIntConst() {
            return intConst;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(intConst.toString());
            InputOutput.write(getType());
        }

    }

    public static class Character{
        // Character → CharConst
        private Token charConst;
        private String type = "<Character>";

        public Character(Token charConst) {
            this.charConst = charConst;
        }

        public Token getCharConst() {
            return charConst;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(charConst.toString());
            InputOutput.write(getType());
        }

    }



    public static class UnaryExp{
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        private PrimaryExp primaryExp;
        private Token ident;
        private Token lParentToken;
        private FuncRParams funcRParams;
        private Token rParentToken;
        private UnaryOp unaryOp;
        private UnaryExp unaryExp;

        private String type = "<UnaryExp>";

        public UnaryExp(PrimaryExp primaryExp, Token ident, Token lParentToken, FuncRParams funcRParams, Token rParentToken, UnaryOp unaryOp, UnaryExp unaryExp) {
            this.primaryExp = primaryExp;
            this.ident = ident;
            this.lParentToken = lParentToken;
            this.funcRParams = funcRParams;
            this.rParentToken = rParentToken;
            this.unaryOp = unaryOp;
            this.unaryExp = unaryExp;
        }

        public PrimaryExp getPrimaryExp() {
            return primaryExp;
        }

        public Token getIdent() {
            return ident;
        }

        public Token getlParentToken() {
            return lParentToken;
        }

        public FuncRParams getFuncRParams() {
            return funcRParams;
        }

        public Token getrParentToken() {
            return rParentToken;
        }

        public UnaryOp getUnaryOp() {
            return unaryOp;
        }

        public UnaryExp getUnaryExp() {
            return unaryExp;
        }

        public String getType() {
            return type;
        }

        public void print(){
            if(primaryExp != null){
                primaryExp.print();
            }
            else if(ident != null){
                InputOutput.write(ident.toString());
                InputOutput.write(lParentToken.toString());
                if(funcRParams != null){
                    funcRParams.print();
                }
                InputOutput.write(rParentToken.toString());
            }
            else {
                unaryOp.print();
                unaryExp.print();
            }
            InputOutput.write(getType());
        }

    }

    public static class UnaryOp{
        // UnaryOp → '+' | '−' | '!'
        private Token token;

        private String type = "<UnaryOp>";

        public UnaryOp(Token token) {
            this.token = token;
        }

        public Token getToken() {
            return token;
        }

        public String getType() {
            return type;
        }

        public void print(){
            InputOutput.write(token.toString());
            InputOutput.write(getType());
        }

    }

    public static class FuncRParams{
        // FuncRParams → Exp { ',' Exp }
        private List<Exp> expList;
        private List<Token> commaTokens;

        private String type = "<FuncRParams>";

        public FuncRParams(List<Exp> expList, List<Token> commaTokens) {
            this.expList = expList;
            this.commaTokens = commaTokens;
        }

        public List<Exp> getExpList() {
            return expList;
        }

        public List<Token> getCommaTokens() {
            return commaTokens;
        }

        public String getType() {
            return type;
        }

        public void print(){
            expList.get(0).print();
            for (int i = 1; i < expList.size(); i++){
                InputOutput.write(commaTokens.get(i - 1).toString());
                expList.get(i).print();
            }
            InputOutput.write(getType());
        }

    }

    public static class MulExp{
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        private UnaryExp unaryExp;
        private MulExp mulExp;
        private Token opToken;
        private String type ="<MulExp>";

        public MulExp(UnaryExp unaryExp, MulExp mulExp, Token opToken) {
            this.unaryExp = unaryExp;
            this.mulExp = mulExp;
            this.opToken = opToken;
        }

        public UnaryExp getUnaryExp() {
            return unaryExp;
        }

        public MulExp getMulExp() {
            return mulExp;
        }

        public Token getOpToken() {
            return opToken;
        }

        public String getType() {
            return type;
        }


        public void print(){
            unaryExp.print();
            InputOutput.write(getType());
            if (opToken != null){
                InputOutput.write(opToken.toString());
                mulExp.print();
            }
        }

    }

    public static class RelExp{
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        private AddExp addExp;
        private RelExp relExp;
        private Token opToken;
        private String type = "<RelExp>";

        public RelExp(AddExp addExp, RelExp relExp, Token opToken) {
            this.addExp = addExp;
            this.relExp = relExp;
            this.opToken = opToken;
        }

        public AddExp getAddExp() {
            return addExp;
        }

        public RelExp getRelExp() {
            return relExp;
        }

        public Token getOpToken() {
            return opToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            addExp.print();
            InputOutput.write(getType());
            if(opToken != null){
                InputOutput.write(opToken.toString());
                relExp.print();
            }
        }

    }

    public static class EqExp{
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        private RelExp relExp;
        private EqExp eqExp;
        private Token opToken;
        private String type = "<EqExp>";

        public EqExp(RelExp relExp, EqExp eqExp, Token opToken) {
            this.relExp = relExp;
            this.eqExp = eqExp;
            this.opToken = opToken;
        }

        public RelExp getRelExp() {
            return relExp;
        }

        public EqExp getEqExp() {
            return eqExp;
        }

        public Token getOpToken() {
            return opToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            relExp.print();
            InputOutput.write(getType());
            if(opToken != null){
                InputOutput.write(opToken.toString());
                eqExp.print();
            }
        }

    }

    public static class LAndExp{
        // LAndExp → EqExp | LAndExp '&&' EqExp
        private EqExp eqExp;
        private LAndExp lAndExp;
        private Token opToken;
        private String type = "<LAndExp>";

        public LAndExp(EqExp eqExp, LAndExp lAndExp, Token opToken) {
            this.eqExp = eqExp;
            this.lAndExp = lAndExp;
            this.opToken = opToken;
        }

        public EqExp getEqExp() {
            return eqExp;
        }

        public LAndExp getlAndExp() {
            return lAndExp;
        }

        public Token getOpToken() {
            return opToken;
        }

        public String getType() {
            return type;
        }

        public void print(){
            eqExp.print();
            InputOutput.write(getType());
            if(opToken != null){
                InputOutput.write(opToken.toString());
                if (lAndExp != null) {
                    lAndExp.print();
                }
            }
        }

    }

}
