package Allocator;

import Debugger.*;
import java.util.*;

class Worker extends Thread {
    private Allocator allocator;

    private Logger logger;

    public Worker() {
        allocator = Allocator.instance;
        logger = Logger.getInstance();
    }

    public void run() {
        while(true) {
            Long address = allocator.allocate(new Random().nextInt(8,10000));
            logger.log(allocator.isAccessible(address) + " na malloc");
            address = allocator.reAllocate(address, new Random().nextInt(8,10000));
            logger.log(allocator.isAccessible(address) + " na realloc");
            allocator.free(address);
            logger.log(allocator.isAccessible(address) + " na free");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        for(int i = 0; i < 10; i++)
            new Worker().start();
    }
}
