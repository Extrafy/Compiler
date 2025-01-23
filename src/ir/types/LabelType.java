package ir.types;

public class LabelType implements Type{
    private final int handler;
    private static int HANDLER = 0;

    public LabelType(){
        handler = HANDLER++;
    }

    public String toString(){
        return "label_" + handler;
    }

    public int getSize() {
        System.out.println("[LabelTypeSize] 非法获取Label类型的Size！");
        return 0;
    }
}
