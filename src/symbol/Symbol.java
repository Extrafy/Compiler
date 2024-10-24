package symbol;

public class Symbol {
    private String name;
    private SymbolType symbolType;
    private int declLineNum;

    public Symbol(String name, SymbolType symbolType, int declLineNum) {
        this.name = name;
        this.symbolType = symbolType;
        this.declLineNum = declLineNum;
    }

    public String getName() {
        return name;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public int getDeclLineNum() {
        return declLineNum;
    }

    public boolean isArray(Symbol symbol){
        return symbol.symbolType == SymbolType.IntArray || symbol.symbolType == SymbolType.CharArray || symbol.symbolType == SymbolType.ConstCharArray || symbol.symbolType == SymbolType.ConstIntArray;
    }

    public String toString(){
        return Integer.toString(declLineNum) + ' ' + name + ' ' + symbolType.toString();
    }
}
