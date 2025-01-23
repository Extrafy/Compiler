package symbol;

import AST.AST;
import error.HandleError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolStack {
    private static final SymbolStack instance = new SymbolStack();
    public static SymbolStack getInstance() {
        return instance;
    }
    public static int loopCount = 0;
    public static int curDeclLineNum = 1;
    private List<SymbolTable> symbolTableStack = new ArrayList<>();// 符号表栈 + 作用域

    public static int getLoopCount() {
        return loopCount;
    }

    public static int getCurDeclLineNum() {
        return curDeclLineNum;
    }

    public void addSymbolTable(boolean isFunc, FuncSymbol.FuncReturnType returnType, int declLineNum, SymbolTable father){
        symbolTableStack.add(new SymbolTable(isFunc, returnType, declLineNum, father));
    }

    public void removeSymbolTable(){
        if (!symbolTableStack.isEmpty()){
            symbolTableStack.remove(symbolTableStack.size() - 1);
        }
        else{
            throw new RuntimeException("栈为空无法删除");
        }
    }

    public SymbolTable getStackTop(){
        if (!symbolTableStack.isEmpty()){
            return symbolTableStack.get(symbolTableStack.size() - 1);
        }
        else {
            throw new RuntimeException("栈为空无法获取栈顶元素");
        }
    }

    public boolean isInCurrent(String ident){  // b:名字重定义
        if(!symbolTableStack.isEmpty()){
            return symbolTableStack.get(symbolTableStack.size() - 1).symbolMap.containsKey(ident);
        }
        else {
            throw new RuntimeException("栈为空无法获取");
        }
    }

    public boolean contains(String ident){  // c:未定义的名字
        for (int i = symbolTableStack.size() - 1; i >= 0; i --){
            if (symbolTableStack.get(i).symbolMap.containsKey(ident)){
                return true;
            }
        }
        return false;
    }

    public boolean isInFunc(){
        for (int i = symbolTableStack.size() - 1; i >= 0 ; i--){
            if (symbolTableStack.get(i).isFunc){
                return true;
            }
        }
        return false;
    }

    public FuncSymbol.FuncReturnType getReturnType(){
        for (int i = symbolTableStack.size() - 1; i>= 0; i--){
            if (symbolTableStack.get(i).isFunc){
                return symbolTableStack.get(i).returnType;
            }
        }
        return null;
    }

    public boolean isCurrentFunc() {
        return symbolTableStack.get(symbolTableStack.size() - 1).isFunc;
    }

    public FuncSymbol.FuncReturnType getCurrentFuncType() {
        return symbolTableStack.get(symbolTableStack.size() - 1).returnType;
    }

    public void put(String ident, Symbol symbol){
        symbolTableStack.get(symbolTableStack.size() - 1).symbolMap.put(ident, symbol);
    }

    public Symbol get(String ident){
        for (int i = symbolTableStack.size() - 1; i >= 0; i--){
            if (symbolTableStack.get(i).symbolMap.containsKey(ident)){
                return symbolTableStack.get(i).symbolMap.get(ident);
            }
        }
        return null;
    }

    public void addCurDeclLineNum(){
        curDeclLineNum++;
    }

    public void inLoop(){
        loopCount++;
    }

    public void outLoop(){
        loopCount--;
    }
}
