package Allocator;

import Debugger.*;
import java.util.*;

class Worker extends Thread {
    private Allocator allocator;

    public Worker() {
        allocator = Allocator.instance;
    }

    public void run() {
        while(true) {
            Long address = allocator.allocate(100);

            try {
                Thread.currentThread().sleep(new Random().nextInt(500,1000));
            } catch(InterruptedException e) {
                Logger.getInstance().log(e.getMessage());
            }

            Logger.getInstance().log(allocator.isAccessible(address));
            allocator.free(address);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        for(int i = 0; i < 10; i++)
            new Worker().start();
    }
}
