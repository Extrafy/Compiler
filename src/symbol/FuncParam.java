package symbol;

public class FuncParam{

    private String name;
    private int dimension; // 0变量, 1数组
    private SymbolType symbolType;


    public FuncParam(String name, int dimension, SymbolType symbolType) {
        this.name = name;
        this.dimension = dimension;
        this.symbolType = symbolType;
    }

    public String getName() {
        return name;
    }

    public int getDimension() {
        return dimension;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public String toString() {
        return "FuncParam{" +
                "name='" + name + '\'' +
                ", dimension=" + dimension +
                '}';
    }
}
