package Debugger;

public class TesterFailedException extends Exception {
    public TesterFailedException(String message) {
        super(message);
    }
    
    public TesterFailedException() {
        super();
    }
}
