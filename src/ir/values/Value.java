package ir.values;

import ir.IRModule;
import ir.types.Type;

import java.util.ArrayList;
import java.util.List;

public class Value {
    private final IRModule module = IRModule.getInstance();

    private String name;

    private Type type;

    private List<Use> useList; // def-use关系

    public static int REG_NUM = 0; // LLWM的寄存器标号

    private final String id; // LLVM 中的 Value 的唯一编号

    public Value(String name, Type type){
        this.name = name;
        this.type = type;
        this.id = UniqueIdGen.getInstance().getUniqueId();
        this.useList = new ArrayList<>();
    }

    public IRModule getModule(){
        return module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Use> getUseList() {
        return useList;
    }

    public void setUseList(List<Use> useList) {
        this.useList = useList;
    }

    public void addUse(Use use){
        this.useList.add(use);
    }

    public void removeUseByUser(User user){
        useList.removeIf(use -> use.getUser() == user);
    }

    public void removeUse(Use use){
        useList.remove(use);
    }

    public String getId() {
        return id;
    }

    public String getUniqueName() {
        if (isNumber()) return getName();
        if (isGlobal()) return getGlobalName();
        return getName() + "_" + getId();
    }

    public boolean isNumber() {
        return this instanceof ConstInt;
    }

    public int getNumber() {
        return Integer.parseInt(name);
    }

    public boolean isGlobal() {
        return name.startsWith("@");
    }

    public String getGlobalName() {
        return name.replaceAll("@", "");
    }


    public String toString(){
        return type.toString()+ " " + name;
    }

    public void replaceUsedWith(Value value){
        List<Use> tmp = new ArrayList<>(useList);
        for (Use use : tmp){
            use.getUser().setOperands(use.getOperandPos(), value);
        }
        this.useList.clear();
    }
}
