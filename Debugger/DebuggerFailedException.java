package Debugger;

public class DebuggerFailedException extends Exception {
    public DebuggerFailedException(String message) {
        super(message);
    }
    
    public DebuggerFailedException() {
        super();
    }
}
