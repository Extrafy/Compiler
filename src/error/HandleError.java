package error;

import utils.InputOutput;

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

    public void addError(Error newError){
        for (Error error : errorList) {
            if (error.equals(newError)) {
                return;
            }
        }
        errorList.add(newError);
    }

    public void printErrors() {
        errorList.sort(Error::compare);
        for (Error error : errorList) {
            InputOutput.writeError(error.toString());
        }
    }
}
