package frontend;

import AST.AST;
import config.Config;
import error.Error;
import error.ErrorType;
import error.HandleError;
import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokenList;
    private int idx = 0;
    private Token curToken;
    private AST ast;
    private static final Parser instance = new Parser();

    public static Parser getInstance(){
        return instance;
    }

    public void setTokenList(List<Token> tokenList){
        this.tokenList = tokenList;
        curToken = tokenList.get(idx);
    }

    public AST getAst(){
        return ast;
    }

    public void analyse(){
        this.ast = new AST(parseCompUnit());
    }

    private Token expect(TokenType tokenType) {
        if (curToken.getTokenType() == tokenType) {
            Token t = curToken;
            if (idx < tokenList.size() - 1) {
                idx++;
                curToken = tokenList.get(idx);
            }
            return t;
        }
        else if (tokenType == TokenType.AND) {
            if (idx < tokenList.size() - 1) {
                idx++;
                curToken = tokenList.get(idx);
            }
            return new Token(TokenType.AND, "&&", curToken.getLine());
        }
        else if (tokenType == TokenType.OR){
            if (idx < tokenList.size() - 1) {
                idx++;
                curToken = tokenList.get(idx);
            }
            return new Token(TokenType.OR, "||", curToken.getLine());
        }
        else if (tokenType == TokenType.SEMICN) {
            Config.errorFlag = true;
            HandleError.getInstance().addError(new Error(tokenList.get(idx - 1).getLine(), ErrorType.i));
            return new Token(TokenType.SEMICN, ";", tokenList.get(idx - 1).getLine());
        }
        else if (tokenType == TokenType.RPARENT) {
            Config.errorFlag = true;
            HandleError.getInstance().addError(new Error(tokenList.get(idx - 1).getLine(), ErrorType.j));
            return new Token(TokenType.RPARENT, ")", tokenList.get(idx - 1).getLine());
        }
        else if (tokenType == TokenType.RBRACK) {
            Config.errorFlag = true;
            HandleError.getInstance().addError(new Error(tokenList.get(idx - 1).getLine(), ErrorType.k));
            return new Token(TokenType.RBRACK, "]" , tokenList.get(idx - 1).getLine());
        }
        else {
            throw new RuntimeException("Syntax error at line " + curToken.getLine() + ": " + curToken.getValue() + " is not " + tokenType);
        }
    }

    private boolean isExp() {
        return curToken.getTokenType() == TokenType.IDENFR ||
                curToken.getTokenType() == TokenType.PLUS ||
                curToken.getTokenType() == TokenType.MINU ||
                curToken.getTokenType() == TokenType.NOT ||
                curToken.getTokenType() == TokenType.LPARENT ||
                curToken.getTokenType() == TokenType.INTCON ||
                curToken.getTokenType() == TokenType.CHRCON;
    }

    public AST.CompUnit parseCompUnit(){
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        List<AST.Decl> declList = new ArrayList<>();
        List<AST.FuncDef> funcDefList = new ArrayList<>();
        AST.MainFuncDef mainFuncDef;
        while (tokenList.get(idx + 1).getTokenType() != TokenType.MAINTK && tokenList.get(idx + 2).getTokenType() != TokenType.LPARENT){
            AST.Decl decl = parseDecl();
            declList.add(decl);
        }
        while (tokenList.get(idx + 1).getTokenType() != TokenType.MAINTK){
            AST.FuncDef funcDef = parseFuncDef();
            funcDefList.add(funcDef);
        }
        mainFuncDef = parseMainFuncDef();
        return new AST.CompUnit(declList, funcDefList, mainFuncDef);
    }

    public AST.Decl parseDecl(){
        // Decl → ConstDecl | VarDecl
        AST.ConstDecl constDecl = null;
        AST.VarDecl varDecl = null;
        if (curToken.getTokenType() == TokenType.CONSTTK){
            constDecl = parseConstDecl();
        }
        else {
            varDecl = parseVarDecl();
        }
        return new AST.Decl(constDecl, varDecl);
    }

    public AST.ConstDecl parseConstDecl(){
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        Token constToken = expect(TokenType.CONSTTK);
        AST.BType bType = parseBType();
        List<AST.ConstDef> constDefList = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        Token semicnToken;
        constDefList.add(parseConstDef());
        while (curToken.getTokenType() == TokenType.COMMA){
            commaTokens.add(expect(TokenType.COMMA));
            constDefList.add(parseConstDef());
        }
        semicnToken = expect(TokenType.SEMICN);
        return new AST.ConstDecl(constToken, bType, constDefList, commaTokens, semicnToken);
    }

    public AST.BType parseBType(){
        // BType → 'int' | 'char'
        Token intToken = null;
        Token charToken = null;
        if (curToken.getTokenType() == TokenType.INTTK){
            intToken = expect(TokenType.INTTK);
        }
        else {
            charToken = expect(TokenType.CHARTK);
        }
        return new AST.BType(intToken, charToken);
    }

    public AST.ConstDef parseConstDef(){
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        Token ident;
        Token lBrackToken = null;
        AST.ConstExp constExp = null;
        Token rBrackToken = null;
        Token assignToken;
        AST.ConstInitVal constInitVal;
        ident = expect(TokenType.IDENFR);
        if(curToken.getTokenType() == TokenType.LBRACK){
            lBrackToken = expect(TokenType.LBRACK);
            constExp = parseConstExp();
            rBrackToken = expect(TokenType.RBRACK);
        }
        assignToken = expect(TokenType.ASSIGN);
        constInitVal = parseConstInitVal();
        return new AST.ConstDef(ident, lBrackToken, constExp, rBrackToken, assignToken, constInitVal);
    }

    public AST.ConstInitVal parseConstInitVal(){
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        List<AST.ConstExp> constExpList = new ArrayList<>();
        Token lBraceToken = null;
        List<Token> commaTokens = new ArrayList<>();
        Token rBraceToken = null;
        Token stringConst = null;
        if(curToken.getTokenType() == TokenType.LBRACE){
            lBraceToken = expect(TokenType.LBRACE);
            if (curToken.getTokenType() != TokenType.RBRACE){
                constExpList.add(parseConstExp());
                while (curToken.getTokenType() != TokenType.RBRACE){
                    commaTokens.add(expect(TokenType.COMMA));
                    constExpList.add(parseConstExp());
                }
            }
            rBraceToken = expect(TokenType.RBRACE);
        }
        else if(curToken.getTokenType() == TokenType.STRCON){
            stringConst = expect(TokenType.STRCON);
        }
        else {
            constExpList.add(parseConstExp());
        }
        return new AST.ConstInitVal(lBraceToken, constExpList, commaTokens, rBraceToken, stringConst);
    }

    public AST.VarDecl parseVarDecl(){
        // VarDecl → BType VarDef { ',' VarDef } ';'
        AST.BType bType = parseBType();
        List<AST.VarDef> varDefList = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        Token semicnToken;
        varDefList.add(parseVarDef());
        while (curToken.getTokenType() == TokenType.COMMA){
            commaTokens.add(expect(TokenType.COMMA));
            varDefList.add(parseVarDef());
        }
        semicnToken = expect(TokenType.SEMICN);
        return new AST.VarDecl(bType, varDefList, commaTokens, semicnToken);
    }

    public AST.VarDef parseVarDef(){
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        Token ident = expect(TokenType.IDENFR);
        Token lBrackToken = null;
        AST.ConstExp constExp = null;
        Token rBrackToken = null;
        Token assignToken = null;
        AST.InitVal initVal = null;
        if (curToken.getTokenType() == TokenType.LBRACK){
            lBrackToken = expect(TokenType.LBRACK);
            constExp = parseConstExp();
            rBrackToken = expect(TokenType.RBRACK);
        }
        if(curToken.getTokenType() == TokenType.ASSIGN){
            assignToken = expect(TokenType.ASSIGN);
            initVal = parseInitVal();
        }
        return new AST.VarDef(ident, lBrackToken, constExp, rBrackToken, assignToken, initVal);
    }

    public AST.InitVal parseInitVal(){
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        Token lBraceToken = null;
        List<AST.Exp> expList = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        Token rBraceToken = null;
        Token stringConst = null;
        if (curToken.getTokenType() == TokenType.LBRACE){
            lBraceToken = expect(TokenType.LBRACE);
            if(curToken.getTokenType() != TokenType.RBRACE){
                expList.add(parseExp());
                while (curToken.getTokenType() != TokenType.RBRACE){
                    commaTokens.add(expect(TokenType.COMMA));
                    expList.add(parseExp());
                }
            }
            rBraceToken = expect(TokenType.RBRACE);
        }
        else if (curToken.getTokenType() == TokenType.STRCON){
            stringConst = expect(TokenType.STRCON);
        }
        else {
            expList.add(parseExp());
        }
        return new AST.InitVal(expList, lBraceToken, commaTokens, rBraceToken, stringConst);
    }

    public AST.FuncDef parseFuncDef(){
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        AST.FuncType funcType = parseFuncType();
        Token ident = expect(TokenType.IDENFR);
        Token lParentToken = expect(TokenType.LPARENT);
        AST.FuncFParams funcFParams = null;
        if (curToken.getTokenType() == TokenType.INTTK || curToken.getTokenType() == TokenType.CHARTK){
            funcFParams = parseFuncFParams();
        }
        Token rParentToken = expect(TokenType.RPARENT);
        AST.Block block = parseBlock();
        return new AST.FuncDef(funcType, ident, lParentToken, funcFParams, rParentToken, block);
    }

    public AST.MainFuncDef parseMainFuncDef(){
        // MainFuncDef → 'int' 'main' '(' ')' Block
        Token intToken = expect(TokenType.INTTK);
        Token mainToken = expect(TokenType.MAINTK);
        Token lParentToken = expect(TokenType.LPARENT);
        Token rParentToken = expect(TokenType.RPARENT);
        AST.Block block = parseBlock();
        return new AST.MainFuncDef(intToken, mainToken, lParentToken, rParentToken, block);
    }

    public AST.FuncType parseFuncType(){
        // FuncType → 'void' | 'int' | 'char'
        Token voidToken = null;
        Token intToken = null;
        Token charToken =null;
        if (curToken.getTokenType() == TokenType.VOIDTK){
            voidToken = expect(TokenType.VOIDTK);
        }
        else if(curToken.getTokenType() == TokenType.INTTK){
            intToken = expect(TokenType.INTTK);
        }
        else {
            charToken = expect(TokenType.CHARTK);
        }
        return new AST.FuncType(voidToken, intToken, charToken);
    }

    public AST.FuncFParams parseFuncFParams(){
        // FuncFParams → FuncFParam { ',' FuncFParam }
        List<AST.FuncFParam> funcFParamList = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        funcFParamList.add(parseFuncFParam());
        while (curToken.getTokenType() == TokenType.COMMA){
            commaTokens.add(expect(TokenType.COMMA));
            funcFParamList.add(parseFuncFParam());
        }
        return new AST.FuncFParams(funcFParamList, commaTokens);
    }

    public AST.FuncFParam parseFuncFParam(){
        // FuncFParam → BType Ident ['[' ']']
        AST.BType bType = parseBType();
        Token indent = expect(TokenType.IDENFR);
        Token lBrackToken = null;
        Token rBrackToken = null;
        if (curToken.getTokenType() == TokenType.LBRACK){
            lBrackToken = expect(TokenType.LBRACK);
            rBrackToken = expect(TokenType.RBRACK);
        }

        return new AST.FuncFParam(bType, indent, lBrackToken, rBrackToken);
    }

    public AST.Block parseBlock(){
        // Block → '{' { BlockItem } '}'
        Token lBraceToken = expect(TokenType.LBRACE);
        List<AST.BlockItem> blockItemList = new ArrayList<>();
        while (curToken.getTokenType() != TokenType.RBRACE){
            blockItemList.add(parseBlockItem());
        }
        Token rBraceToken = expect(TokenType.RBRACE);
        return new AST.Block(lBraceToken, blockItemList, rBraceToken);
    }

    public AST.BlockItem parseBlockItem(){
        // BlockItem → Decl | Stmt
        AST.Decl decl = null;
        AST.Stmt stmt = null;
        if (curToken.getTokenType() == TokenType.CONSTTK || curToken.getTokenType() == TokenType.INTTK || curToken.getTokenType() == TokenType.CHARTK){
            decl = parseDecl();
        }
        else {
            stmt = parseStmt();
        }
        return new AST.BlockItem(decl, stmt);
    }

    public AST.Stmt parseStmt(){
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
        if (curToken.getTokenType() == TokenType.LBRACE){
            // Block
            AST.Block block = parseBlock();
            return new AST.Stmt(AST.Stmt.StmtType.Block, block);
        }
        else if(curToken.getTokenType() == TokenType.IFTK){
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            Token ifToken = expect(TokenType.IFTK);
            Token lParentToken = expect(TokenType.LPARENT);
            AST.Cond cond = parseCond();
            Token rParentToken = expect(TokenType.RPARENT);
            List<AST.Stmt> stmtList = new ArrayList<>();
            stmtList.add(parseStmt());
            Token elseToken = null;
            if (curToken.getTokenType() == TokenType.ELSETK){
                elseToken = expect(TokenType.ELSETK);
                stmtList.add(parseStmt());
            }
            return new AST.Stmt(AST.Stmt.StmtType.If, ifToken, lParentToken, cond, rParentToken, stmtList, elseToken);
        }
        else if (curToken.getTokenType() == TokenType.FORTK){
            // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            Token forToken = expect(TokenType.FORTK);
            Token lParentToken = expect(TokenType.LPARENT);
            AST.ForStmt forStmt1 = null;
            AST.ForStmt forStmt2 = null;
            if (curToken.getTokenType() != TokenType.SEMICN){
                forStmt1 = parseForStmt();
            }
            Token semicnToken1 = expect(TokenType.SEMICN);
            AST.Cond cond = null;
            if (curToken.getTokenType() != TokenType.SEMICN){
                cond = parseCond();
            }
            Token semicnToken2 = expect(TokenType.SEMICN);
            if(curToken.getTokenType() != TokenType.RPARENT){
                forStmt2 = parseForStmt();
            }
            Token rParentToken = expect(TokenType.RPARENT);
            List<AST.Stmt> stmtList = new ArrayList<>();
            stmtList.add(parseStmt());
            return new AST.Stmt(AST.Stmt.StmtType.For, semicnToken1, semicnToken2, lParentToken, cond, rParentToken, stmtList, forToken, forStmt1, forStmt2);
        }
        else if(curToken.getTokenType() == TokenType.BREAKTK){
            // 'break' ';'
            Token breakToken = expect(TokenType.BREAKTK);
            Token semicnToken = expect(TokenType.SEMICN);
            return new AST.Stmt(AST.Stmt.StmtType.Break, semicnToken, breakToken);
        }
        else if(curToken.getTokenType() == TokenType.CONTINUETK){
             // 'continue' ';'
            Token continueToken = expect(TokenType.CONTINUETK);
            Token semicnToken = expect(TokenType.SEMICN);
            return new AST.Stmt(AST.Stmt.StmtType.Continue, semicnToken, continueToken);
        }
        else if(curToken.getTokenType() == TokenType.RETURNTK){
            // 'return' [Exp] ';'
            Token returnToken = expect(TokenType.RETURNTK);
            AST.Exp exp = null;
            if (isExp()){
                exp = parseExp();
            }
            Token semicnToken = expect(TokenType.SEMICN);
            return new AST.Stmt(AST.Stmt.StmtType.Return, exp, semicnToken, returnToken);
        }
        else if (curToken.getTokenType() == TokenType.PRINTFTK){
            // 'printf' '('StringConst {','Exp}')'';'
            Token printfToken = expect(TokenType.PRINTFTK);
            Token lParentToken = expect(TokenType.LPARENT);
            Token stringConst = expect(TokenType.STRCON);
            List<Token> commaTokens = new ArrayList<>();
            List<AST.Exp> expList = new ArrayList<>();
            while (curToken.getTokenType() == TokenType.COMMA){
                commaTokens.add(expect(TokenType.COMMA));
                expList.add(parseExp());
            }
            Token rParentToken = expect(TokenType.RPARENT);
            Token semicnToken = expect(TokenType.SEMICN);
            return new AST.Stmt(AST.Stmt.StmtType.Printf, semicnToken, lParentToken, rParentToken, printfToken, stringConst, commaTokens, expList);
        }
        else {
            int assign = idx;
            for (int i = idx; i < tokenList.size() && tokenList.get(i).getLine() == curToken.getLine(); i++){
                if(tokenList.get(i).getTokenType() == TokenType.ASSIGN){
                    assign = i;
                }
            }
            if (assign > idx){
                // LVal '=' Exp ';'
                // LVal '=' 'getint''('')'';'
                // LVal '=' 'getchar''('')'';'
                AST.LVal lVal = parseLval();
                Token assignToken = expect(TokenType.ASSIGN);
                if(curToken.getTokenType() == TokenType.GETINTTK){
                    Token getintToken = expect(TokenType.GETINTTK);
                    Token lParentToken = expect(TokenType.LPARENT);
                    Token rParentToken = expect(TokenType.RPARENT);
                    Token semicnToken = expect(TokenType.SEMICN);
                    return new AST.Stmt(AST.Stmt.StmtType.LValAssignGetint, lVal, assignToken, semicnToken, lParentToken, rParentToken, getintToken);
                }
                else if(curToken.getTokenType() == TokenType.GETCHARTK){
                    Token getcharToken = expect(TokenType.GETCHARTK);
                    Token lParentToken = expect(TokenType.LPARENT);
                    Token rParentToken = expect(TokenType.RPARENT);
                    Token semicnToken = expect(TokenType.SEMICN);
                    return new AST.Stmt(AST.Stmt.StmtType.LValAssignGetchar, lVal, assignToken, semicnToken, lParentToken, rParentToken, getcharToken);
                }
                else {
                    AST.Exp exp = parseExp();
                    Token semicnToken = expect(TokenType.SEMICN);
                    return new AST.Stmt(AST.Stmt.StmtType.LValAssignExp, lVal, assignToken, exp, semicnToken);
                }
            }
            else {
                // [Exp] ';'
                AST.Exp exp = null;
                if (isExp()){
                    exp = parseExp();
                }
                Token semicnToken = expect(TokenType.SEMICN);
                return new AST.Stmt(AST.Stmt.StmtType.Exp, exp, semicnToken);
            }
        }
    }


    public AST.ForStmt parseForStmt(){
        // ForStmt → LVal '=' Exp
        AST.LVal lVal = parseLval();
        Token assignToken = expect(TokenType.ASSIGN);
        AST.Exp exp = parseExp();
        return new AST.ForStmt(lVal, assignToken, exp);
    }

    public AST.Exp parseExp(){
        // Exp → AddExp
        AST.AddExp addExp = parseAddExp();
        return new AST.Exp(addExp);
    }

    public AST.Cond parseCond(){
        // Cond → LOrExp
        AST.LOrExp lOrExp = parseLOrExp();
        return new AST.Cond(lOrExp);
    }

    public AST.LVal parseLval(){
        // LVal → Ident ['[' Exp ']']
        Token ident = expect(TokenType.IDENFR);
        Token lBrackToken = null;
        AST.Exp exp = null;
        Token rBrackToken = null;
        if (curToken.getTokenType() == TokenType.LBRACK){
            lBrackToken = expect(TokenType.LBRACK);
            exp = parseExp();
            rBrackToken = expect(TokenType.RBRACK);
        }
        return new AST.LVal(ident, lBrackToken, exp, rBrackToken);
    }

    public AST.PrimaryExp parsePrimaryExp() {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        Token lParentToken = null;
        AST.Exp exp = null;
        Token rParentToken = null;
        AST.LVal lVal = null;
        AST.Number number = null;
        AST.Character character = null;
        if (curToken.getTokenType() == TokenType.LPARENT){
            lParentToken = expect(TokenType.LPARENT);
            exp = parseExp();
            rParentToken = expect(TokenType.RPARENT);
        }
        else if (curToken.getTokenType() == TokenType.INTCON){
            number = parseNumber();
        }
        else if (curToken.getTokenType() == TokenType.CHRCON){
            character = parseCharacter();
        }
        else {
            lVal = parseLval();
        }
        return new AST.PrimaryExp(lParentToken, exp, rParentToken, lVal, number, character);
    }

    public AST.Number parseNumber() {
        // Number → IntConst
        return new AST.Number(expect(TokenType.INTCON));
    }

    public AST.Character parseCharacter(){
        // Character → CharConst
        return new AST.Character(expect(TokenType.CHRCON));
    }

    public AST.UnaryExp parseUnaryExp(){
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        AST.PrimaryExp primaryExp = null;
        Token ident = null;
        Token lParentToken = null;
        AST.FuncRParams funcRParams = null;
        Token rParentToken = null;
        AST.UnaryOp unaryOp = null;
        AST.UnaryExp unaryExp = null;
        if(curToken.getTokenType() == TokenType.IDENFR && tokenList.get(idx + 1).getTokenType() == TokenType.LPARENT){
            ident = expect(TokenType.IDENFR);
            lParentToken = expect(TokenType.LPARENT);
            if(isExp()){
                funcRParams = parseFuncRParams();
            }
            rParentToken = expect(TokenType.RPARENT);
        }
        else if (curToken.getTokenType() == TokenType.PLUS || curToken.getTokenType() == TokenType.MINU || curToken.getTokenType() == TokenType.NOT){
            unaryOp = parseUnaryOp();
            unaryExp = parseUnaryExp();
        }
        else {
            primaryExp = parsePrimaryExp();
        }
        return new AST.UnaryExp(primaryExp, ident, lParentToken, funcRParams, rParentToken, unaryOp, unaryExp);
    }

    public AST.UnaryOp parseUnaryOp(){
        // UnaryOp → '+' | '−' | '!'
        Token token;
        if (curToken.getTokenType() == TokenType.PLUS){
            token = expect(TokenType.PLUS);
        }
        else if (curToken.getTokenType() == TokenType.MINU){
            token = expect(TokenType.MINU);
        }
        else {
            token = expect(TokenType.NOT);
        }
        return new AST.UnaryOp(token);
    }

    public AST.FuncRParams parseFuncRParams(){
        // FuncRParams → Exp { ',' Exp }
        List<AST.Exp> expList = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        expList.add(parseExp());
        while (curToken.getTokenType() == TokenType.COMMA){
            commaTokens.add(expect(TokenType.COMMA));
            expList.add(parseExp());
        }
        return new AST.FuncRParams(expList, commaTokens);
    }

    public AST.MulExp parseMulExp(){
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        AST.UnaryExp unaryExp = parseUnaryExp();
        Token op = null;
        AST.MulExp mulExp = null;
        if (curToken.getTokenType() == TokenType.MULT){
            op = expect(TokenType.MULT);
            mulExp = parseMulExp();
        }
        else if(curToken.getTokenType() == TokenType.DIV){
            op = expect(TokenType.DIV);
            mulExp = parseMulExp();
        }
        else if(curToken.getTokenType() == TokenType.MOD){
            op = expect(TokenType.MOD);
            mulExp = parseMulExp();
        }
        return new AST.MulExp(unaryExp, mulExp, op);
    }

    public AST.AddExp parseAddExp(){
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        AST.MulExp mulExp = parseMulExp();
        Token op = null;
        AST.AddExp addExp = null;
        if (curToken.getTokenType() == TokenType.PLUS){
            op = expect(TokenType.PLUS);
            addExp = parseAddExp();
        }
        else if (curToken.getTokenType() == TokenType.MINU) {
            op = expect(TokenType.MINU);
            addExp = parseAddExp();
        }
        return new AST.AddExp(mulExp, addExp, op);
    }

    public AST.RelExp parseRelExp(){
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        AST.AddExp addExp = parseAddExp();
        Token op = null;
        AST.RelExp relExp = null;
        if (curToken.getTokenType() == TokenType.LSS){
            op = expect(TokenType.LSS);
            relExp = parseRelExp();
        }
        else if (curToken.getTokenType() == TokenType.GRE){
            op = expect(TokenType.GRE);
            relExp = parseRelExp();
        }
        else if (curToken.getTokenType() == TokenType.LEQ){
            op = expect(TokenType.LEQ);
            relExp = parseRelExp();
        }
        else if (curToken.getTokenType() == TokenType.GEQ){
            op = expect(TokenType.GEQ);
            relExp = parseRelExp();
        }
        return new AST.RelExp(addExp, relExp, op);
    }

    public AST.EqExp parseEqExp(){
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        AST.RelExp relExp = parseRelExp();
        Token op = null;
        AST.EqExp eqExp = null;
        if (curToken.getTokenType() == TokenType.EQL){
            op = expect(TokenType.EQL);
            eqExp = parseEqExp();
        }
        else if (curToken.getTokenType() == TokenType.NEQ){
            op = expect(TokenType.NEQ);
            eqExp = parseEqExp();
        }
        return new AST.EqExp(relExp, eqExp, op);
    }

    public AST.LAndExp parseLAndExp(){
        // LAndExp → EqExp | LAndExp '&&' EqExp
        AST.EqExp eqExp = parseEqExp();
        Token op = null;
        AST.LAndExp lAndExp = null;
        if (curToken.getTokenType() == TokenType.AND || curToken.getTokenType() == TokenType.ERROR){
            op = expect(TokenType.AND);
            lAndExp = parseLAndExp();
        }
        return new AST.LAndExp(eqExp, lAndExp, op);
    }

    public AST.LOrExp parseLOrExp(){
        // LOrExp → LAndExp | LOrExp '||' LAndExp
        AST.LAndExp lAndExp = parseLAndExp();
        Token op = null;
        AST.LOrExp lOrExp = null;
        if (curToken.getTokenType() == TokenType.OR || curToken.getTokenType() == TokenType.ERROR){
            op = expect(TokenType.OR);
            lOrExp = parseLOrExp();
        }
        return new AST.LOrExp(lAndExp, lOrExp, op);
    }

    public AST.ConstExp parseConstExp(){
        // ConstExp → AddExp
        return new AST.ConstExp(parseAddExp());
    }

    public void printParseAnswer(){
        ast.print();
    }
}
