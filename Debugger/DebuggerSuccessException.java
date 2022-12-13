package Debugger;

public class DebuggerSuccessException extends Exception {
    public DebuggerSuccessException(String message) {
        super(message);
    }
    
    public DebuggerSuccessException() {
        super();
    }
}
