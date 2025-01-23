package symbol;

public class VarSymbol extends Symbol{
    private boolean isConst;
    private int dimension; // 0变量 , 1数组

    public VarSymbol(String name, SymbolType symbolType, int num, boolean isConst, int dimension) {
        super(name, symbolType, num);
        this.isConst = isConst;
        this.dimension = dimension;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDimension() {
        return dimension;
    }
}
