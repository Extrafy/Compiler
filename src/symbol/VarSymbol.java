package symbol;

public class VarSymbol extends Symbol{
    private boolean isConst;
    private boolean isStatic;
    private int dimension; // 0变量 , 1数组

    public VarSymbol(String name, SymbolType symbolType, int num, boolean isConst, boolean isStatic, int dimension) {
        super(name, symbolType, num);
        this.isStatic = isStatic;
        this.isConst = isConst;
        this.dimension = dimension;
    }

    public boolean isStatic(){
        return isStatic;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDimension() {
        return dimension;
    }
}
