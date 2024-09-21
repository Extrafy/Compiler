package error;

import java.util.ArrayList;
import java.util.List;

public class HandleError {
    private static final HandleError instance = new HandleError();

    private List<Error> errorList = new ArrayList<Error>();

    public static HandleError getInstance() {
        return instance;
    }

    public List<Error> getErrorList() {
        return errorList;
    }

    public void addError(Error error){
        this.errorList.add(error);
    }
}
