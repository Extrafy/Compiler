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

    public boolean equals(Error error) {
        return errorLine == error.errorLine;
    }

    public int compareTo(Error error) {
        if (equals(error)) {
            return 0;
        }
        else if (errorLine < error.errorLine) {
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        return Integer.toString(errorLine) + ' ' + errorType.toString();
    }
}
