package Allocator;

import java.util.Random;

class Worker extends Thread {
    private static int count = 0;

    private Allocator allocator;

    private Random random;

    private int id;

    private Logger logger;
    
    public Worker() {
        allocator = AllocatorImplementation.getInstance();
        random = new Random();
        id = count++;
        logger = Logger.getInstance();
    }

    public void run() {
        logger.log(id + " run");
        
        while(true) {
            int amount = random.nextInt(8,10000);

            Long address = allocator.allocate(amount);
            // logger.log("Allocated " + amount + " bytes at " + address + " [" + id + "]");

            try {
                Thread.sleep(random.nextInt(250,2000));
            } catch (InterruptedException e) {}

            logger.log(allocator.isAccessible(address) + " [" + id + "]");
            
            try {
                Thread.sleep(random.nextInt(250,2000));
            } catch (InterruptedException e) {}

            allocator.free(address);
            // logger.log("Freed " + amount + " bytes at " + address + " [" + id + "]");
        }
    }
}

public class AllocatorMain {
    public static void main(String[] args) {
        for(int i = 0; i < 10 ; i++) {
            new Worker().start();
        }
    }   
}
