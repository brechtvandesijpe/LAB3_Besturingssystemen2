package Allocator;

import java.util.concurrent.*;

/**
 * 
 * Semaphore designed for a readers-writers problem. Readers can access together, but writers have to wait for readers to finish.
 * Neverthless, once there are more then a given amount of readers inside the critical section, access will be denied when a writer
 * is waiting for access. This way writers don't starve too much.
 * 
 */

public class RWSemaphore {
    private Semaphore roomEmpty;

    private Semaphore mutex;

    private int readers;

    public RWSemaphore(int waitingWritersLimit) {
        roomEmpty = new Semaphore(1);
        mutex = new Semaphore(1);

        readers = 0;
    }

    public void enterReader() {
        try {
            mutex.acquire();
            readers++;
            if(readers == 1)
                roomEmpty.acquire();
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enterWriter() {
        try {
            roomEmpty.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void leaveReader() {
        try {
            mutex.acquire();
            readers--;
            if(readers == 0)
                roomEmpty.release();
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void leaveWriter() {
        roomEmpty.release();
    }
}
