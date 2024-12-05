package backend.instructions;

public class MipsAnnotation extends MipsInstruction{
    private String content;

    public MipsAnnotation(String content) {
        this.content = content;
    }

    public String toString() {
        return "# " + content + "\n";
    }
}
