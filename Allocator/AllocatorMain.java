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
<<<<<<< Updated upstream
            // logger.log("Freed " + amount + " bytes at " + address + " [" + id + "]");
=======
            logger.log("Freed " + amount + " bytes at " + address);
        }
    }
}

class BlockWorker extends Thread {
    private Block block;

    private Logger logger;

    private Random random;
    
    public BlockWorker(Block b) {
        block = b;
        logger = Logger.getInstance();
        random = new Random();
    }

    public void run() {
        while(true) {
            Long address = block.getPage();

            try {
                Thread.currentThread().sleep(random.nextInt(250,2500));
            } catch (InterruptedException e) {}

            logger.log("freepages: " + block.hasFreePages());

            try {
                Thread.currentThread().sleep(random.nextInt(250,2500));
            } catch (InterruptedException e) {}

            logger.log("accessible: " + block.isAccessible(address));

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

    private Random random;
    
    public ArenaWorker(Arena a) {
        arena = a;
        logger = Logger.getInstance();
        random = new Random();
    }

    public void run() {
        while(true) {
            Long address = arena.getPage();

            try {
                Thread.currentThread().sleep(random.nextInt(250,2500));
            } catch (InterruptedException e) {}

            logger.log("accessible: " + arena.isAccessible(address));

            try {
                Thread.currentThread().sleep(random.nextInt(250,2500));
            } catch (InterruptedException e) {}

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
            boolean b = allocator.isAccessible(address);
            /*if(!b)*/ logger.log(b + "  b"/*" address was not null " + this*/);
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

public class AllocatorMain {
    public static void main(String[] args) {
        Block block = new Block((BackingStore.getInstance().mmap(4096)), 8, 4096);
        Arena arena = new Arena(8);

        LinkedList<Wrapper> wrappers = new LinkedList<Wrapper>();
        for(int i = 0; i < 2; i++) {
            wrappers.add(new Wrapper());
>>>>>>> Stashed changes
        }

<<<<<<< Updated upstream
public class AllocatorMain {
    public static void main(String[] args) {
        for(int i = 0; i < 10 ; i++) {
            new Worker().start();
        }
    }   
=======
        for(int i = 0; i < 5; i++) {
            // new BlockWorker(block).start();
            // new ArenaWorker(arena).start();
            // new Wrapper().start();
            new AllocatorWorker(wrappers).start();
        }
    }
>>>>>>> Stashed changes
}
