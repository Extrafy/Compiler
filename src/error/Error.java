package error;

public class Error {
    public int errorLine;
    public ErrorType errorType;

    public Error(int line, ErrorType errorType){
        this.errorLine = line;
        this.errorType = errorType;
    }

    public void setErrorLine(int errorLine) {
        this.errorLine = errorLine;
    }

    public int getErrorLine() {
        return errorLine;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return Integer.toString(errorLine) + ' ' + errorType.toString();
    }
}
