package symbol;

import java.util.List;

public class FuncSymbol extends Symbol {
    public static enum FuncReturnType  {
        VOID, INT, CHAR
    }

    private FuncReturnType returnType;
    private List<FuncParam> funcParams;

    public FuncSymbol(String name, SymbolType symbolType, int num, FuncReturnType returnType, List<FuncParam> params) {
        super(name, symbolType, num);
        this.returnType = returnType;
        this.funcParams = params;
    }

    public FuncReturnType getReturnType() {
        return returnType;
    }

    public List<FuncParam> getFuncParams() {
        return funcParams;
    }
}
