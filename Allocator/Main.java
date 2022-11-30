package Allocator;

import Debugger.*;

public class Main {
    private static Long address;

    private static Long oldAddress;

    private static int size;

    private static int oldSize;

    private static Allocator allocator;

    private static Logger logger;

    public static void ensureAllocated(boolean condition) {
        // Check if all addresses are accessible
        for(Long i = address; i < address + size; i++) {
            if(allocator.isAccessible(i) != condition) {
                if(condition)
                    logger.log("Address " + i + " is not allocated while it should be");
                else
                    logger.log("Address " + i + " is allocated while it shouldn't be");
            }
        }

        // If reallocating, check if the old addresses are freed
        if(oldSize != 0 && oldAddress != null) {
            for(Long i = address; i < address + oldSize; i++) {
                if(allocator.isAccessible(i) == condition) {
                    if(condition)
                        logger.log("Address " + i + " is not allocated while it should be");
                    else
                        logger.log("Address " + i + " is allocated while it shouldn't be");
                }
            }
        }

    }
    
    public static void main(String[] args) {
        // Create a new allocator
        allocator = new STAllocator();
        logger = Logger.getInstance();
        
        oldAddress = null;
        oldSize = 0;
    
        for(int i = 8; i <= 10000; i++) {
            size = i;
            // Allocate a new size
            address = allocator.allocate(size);
            ensureAllocated(true);
    
            oldAddress = address;
            oldSize = size;
            address = allocator.reAllocate(address, size / 2);
            ensureAllocated(true);
    
            oldAddress = address;
            oldSize = size;
            address = allocator.reAllocate(address, size * 2);
            ensureAllocated(true);
    
            // Free the allocated memory
            allocator.free(address);
            ensureAllocated(false);
        }

        for(int i = 0; i < 100; i++) {
            logger.log(i + ":" + round(i));
        }
    }
    
    public static int round(int x) {
        return (int) );
    }
}
