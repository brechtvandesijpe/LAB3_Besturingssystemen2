package Allocator;
public class EmptyBlockException extends Exception {
    public EmptyBlockException(String message) {
        super(message);
    }
    
    public EmptyBlockException() {
        super();
    }
}
