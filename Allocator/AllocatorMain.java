package Allocator;

import java.util.*;

import Debugger.Logger;

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
        allocator = new MTAllocator();
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
                Thread.sleep(random.nextInt(250,500));
            } catch (InterruptedException e) {}

            logger.log(allocator.isAccessible(address));
            
            try {
                Thread.sleep(random.nextInt(250,500));
            } catch (InterruptedException e) {}

            allocator.free(address);
            logger.log("Freed " + amount + " bytes at " + address);
        }
    }
}

class BlockWorker extends Thread {
    private Block block;

    private Logger logger;
    
    public BlockWorker(Block b) {
        block = b;
        logger = Logger.getInstance();
    }

    public void run() {
        while(true) {
            Long address = block.getPage();

            logger.log(block.hasFreePages());
            logger.log(block.isAccessible(address));

            try {
                block.freePage(address);
            } catch(EmptyBlockException e) {
                logger.log(e.getMessage());
            }
        }
    }
}

class ArenaWorker extends Thread {
    private Arena arena;

    private Logger logger;
    
    public ArenaWorker(Arena a) {
        arena = a;
        logger = Logger.getInstance();
    }

    public void run() {
        while(true) {
            Long address = arena.getPage();
            arena.isAccessible(address);
            arena.freePage(address);
        }
    }
}

class Wrapper /*extends Thread */{
    private Logger logger;

    private Allocator allocator;

    private Long address;

    private Random random;

    public Wrapper() {
        logger = Logger.getInstance();
        allocator = Allocator.instance;
        address = null;
        random = new Random();
    }

    public void run() {
        if(address == null) {
            int size = random.nextInt(8, 10000);
            address = allocator.allocate(size);
        } else {
            logger.log(allocator.isAccessible(address) + "  b"/*" address was not null " + this*/);
            allocator.free(address);
            // logger.log(allocator.isAccessible(address) + " a"/*" address was not null " + this*/);
            address = null;
        }
    }
}

class AllocatorWorker extends Thread {
    private LinkedList<Wrapper> wrappers;
    
    private Logger logger;

    private Random random;

    public AllocatorWorker(LinkedList<Wrapper> w) {
        wrappers = w;
        logger = Logger.getInstance();
        random = new Random();
    }

    public void run() {
        while(true) {
            synchronized(wrappers) {
                int i = random.nextInt(0, wrappers.size());
                wrappers.get(i).run();
            }
        }
    }
}

class Test {
    public Test() {}

    public void test() {
        Logger logger = Logger.getInstance();
        logger.log(this);

        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for(StackTraceElement e : stackTraceElements) {
            logger.log(e);
        }
    }
}

public class AllocatorMain {
    public static void main(String[] args) {
        // Block block = new Block((BackingStore.getInstance().mmap(4096)), 8, 4096);
        // Arena arena = new Arena(8);

        // LinkedList<Wrapper> wrappers = new LinkedList<Wrapper>();
        // for(int i = 0; i < 2; i++) {
        //     wrappers.add(new Wrapper());
        // }

        // for(int i = 0; i < 10; i++) {
        //     // new BlockWorker(block).start();
        //     // new ArenaWorker(arena).start();
        //     // new Wrapper().start();
        //     new AllocatorWorker(wrappers).start();
        // }
        Test t = new Test();
        t.test();
    }
}
