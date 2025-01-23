package backend.operands;

import java.util.Objects;

public class MipsLabel extends MipsOperand{
    private String name;

    public MipsLabel(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsLabel objLabel = (MipsLabel) o;
        return Objects.equals(name, objLabel.name);
    }

    public String toString() {
        return name;
    }

}
