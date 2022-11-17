package Allocator;

import java.util.*;

class Worker extends Thread {
    private static int count = 0;

    private Allocator allocator;

    private Random random;

    private int id;
    
    public Worker() {
        allocator = AllocatorImplementation.getInstance();
        random = new Random();
        id = count++;
    }

    public void run() {
        int amount = random.nextInt(8,10000);

        Long address = allocator.allocate(amount);
        System.out.println("Allocated " + amount + " bytes at " + address + " [" + id + "]");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        System.out.println(allocator.isAccessible(address) + " [" + id + "]");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        allocator.free(address);
        System.out.println("Freed " + amount + " bytes at " + address + " [" + id + "]");
    }
}

public class Main {
 
    public static void main(String[] args) {
        
        for(int i = 0; i < 5; i++) {
            new Worker().start();
        }

    }

}