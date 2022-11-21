package Allocator;

import java.util.*;

class Worker extends Thread {
    private static int count = 0;

    private Allocator allocator;

    private Random random;

    private int id;

    private Logger logger;
    
    public Worker() {
        random = new Random();
        id = count++;
        logger = Logger.getInstance();
        allocator = new STAllocator();
    }

    public void run() {
        logger.log(id + " run");
        
        while(true) {
            int amount = random.nextInt(8,10000);

            Long address = allocator.allocate(amount);
            logger.log("Allocated " + amount + " bytes at " + address);

            try {
                Thread.sleep(random.nextInt(250,2000));
            } catch (InterruptedException e) {}

            logger.log(allocator.isAccessible(address));

            amount = random.nextInt(8,10000);

            address = allocator.reAllocate(address, amount);
            logger.log("Allocated " + amount + " bytes at " + address);

            try {
                Thread.sleep(random.nextInt(250,2000));
            } catch (InterruptedException e) {}

            logger.log(allocator.isAccessible(address));
            
            try {
                Thread.sleep(random.nextInt(250,2000));
            } catch (InterruptedException e) {}

            allocator.free(address);
            logger.log("Freed " + amount + " bytes at " + address);
        }
    }
}

public class AllocatorMain {
    public static void main(String[] args) {
        for(int i = 0; i < 1; i++) {
            new Worker().start();
        }
    }   
}
