package Allocator;

import java.util.Random;

public class Main {
    private static Long address;

    private static Allocator allocator;

    private static Logger logger;

    private static int size;

    public static void ensureAllocated(boolean condition) {
        if(allocator.isAccessible(address) != condition) {
            if(condition)
                logger.log("Address " + address + " is not allocated while it should be");
            else
                logger.log("Address " + address + " is allocated while it shouldn't be");
        }
    }

    public static void main(String[] args) {
        // Create a new allocator
        allocator = new STAllocator();
        logger = Logger.getInstance();
        Random random = new Random();

        while(true) {
            size = random.nextInt(8, 10000);
            // Allocate a new size
            address = allocator.allocate(size);
            ensureAllocated(true);

            address = allocator.reAllocate(address, size / 2);
            ensureAllocated(true);

            address = allocator.reAllocate(address, size * 4);
            ensureAllocated(true);

            // Free the allocated memory
            allocator.free(address);
            ensureAllocated(false);
        }
    }
}
