package backend.instructions;

public class MipsMacro extends MipsInstruction{
    // 宏调用或者注释
    private String content;

    public MipsMacro(String content) {
        this.content = content;
    }
    public String toString() {
        return content + "\n";
    }
}
