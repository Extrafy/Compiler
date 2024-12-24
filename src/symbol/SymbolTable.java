package symbol;

import utils.InputOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SymbolTable {
    private static final SymbolTable root = new SymbolTable(false, null, 0, null);
    public static SymbolTable getRootSymbolTable(){
        return root;
    }
    public LinkedHashMap<String, Symbol> symbolMap;
    public boolean isFunc;
    public FuncSymbol.FuncReturnType returnType;
    public int declLineNum;
    public SymbolTable father;
    public ArrayList<SymbolTable> sons;

    public SymbolTable(boolean isFunc, FuncSymbol.FuncReturnType returnType, int declLineNum, SymbolTable father) {
        this.symbolMap = new LinkedHashMap<>();
        this.isFunc = isFunc;
        this.returnType = returnType;
        this.declLineNum = declLineNum;
        this.father = father;
        this.sons = new ArrayList<>();
    }

    public HashMap<String, Symbol> getSymbolMap() {
        return symbolMap;
    }

    public boolean isFunc() {
        return isFunc;
    }

    public FuncSymbol.FuncReturnType getReturnType() {
        return returnType;
    }

    public int getDeclLineNum() {
        return declLineNum;
    }

    public void addSon(SymbolTable symbolTable){
        sons.add(symbolTable);
    }

    public void printSymbols(){
        for (Symbol symbol : symbolMap.values()) {
            InputOutput.writeSymbol(symbol.toString());
        }
        if (!sons.isEmpty()){
            for (SymbolTable son : sons){
                son.printSymbols();
            }
        }
    }
}
