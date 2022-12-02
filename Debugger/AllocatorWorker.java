package Debugger;

import Allocator.Allocator;
import Allocator.AllocatorException;
import java.util.Random;

public class AllocatorWorker extends Thread {
    private static Random random = new Random();

    private Allocator allocator;

    private int amountOfIterations;

    private Logger logger;

    public AllocatorWorker(Allocator allocator, int amountOfIterations) {
        this.allocator = allocator;
        this.amountOfIterations = amountOfIterations;
        logger = Logger.getInstance();
    }

    public void testRange(Long startAddress, int range, boolean condition) throws RuntimeException {
        if(allocator.isAccessible(startAddress, range) != condition) {
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range), 1);
            throw new RuntimeException();
        }
    }

    public void run() throws RuntimeException {
        for(int i = 0; i < amountOfIterations; i++) {
            try {
                Long address = allocator.allocate(random.nextInt(8,10000));
                testRange(address, 1, true);
                address = allocator.reAllocate(address, random.nextInt(8,10000));
                testRange(address, 1, true);
                allocator.free(address);
                testRange(address, 1, false);
            } catch(AllocatorException e) {}
        }
    }
}