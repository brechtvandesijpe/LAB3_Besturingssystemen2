package Allocator;

import java.util.*;

class Data {
    private int data;

    public Data(Integer data) {
        this.data = data;
    }

    public int getData() {
        return data;
    }

    public void increment() {
        data++;
    }
}

class Reader extends Thread {
    private static int count = 0;
    
    private int id;

    private RWSemaphore semaphore;

    private Logger logger;

    private Data data;

    public Reader(RWSemaphore semaphore, Data data) {
        id = count++;
        this.semaphore = semaphore;
        logger = Logger.getInstance();
        this.data = data;
    }

    public void run() {
        while(true) {
            semaphore.enterReader();
            logger.log("Reader " + id + " is reading: " + data.getData());
            semaphore.leaveReader();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }
}

class Writer extends Thread {
    private static int count = 0;
    
    private int id;

    private RWSemaphore semaphore;

    private Logger logger;
    
    private Data data;

    public Writer(RWSemaphore semaphore, Data data) {
        id = count++;
        this.semaphore = semaphore;
        logger = Logger.getInstance();
        this.data = data;
    }

    public void run() {
        while(true) {
            semaphore.enterWriter();
            data.increment();
            logger.log("Writer " + id + " is writing: " + data.getData());
            semaphore.leaveWriter();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }
}

public class RWMain {
    public static void main(String[] args) {

        RWSemaphore semaphore = new RWSemaphore(3);

        Data data = new Data(0);

        for(int i = 0; i < 3; i++) {
            new Reader(semaphore, data).start();
            new Writer(semaphore, data).start();
        }

    }

}