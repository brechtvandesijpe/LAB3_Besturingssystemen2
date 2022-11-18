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

    private Semaphore readersIncrement;

    private Semaphore writersIncrement;

    private Semaphore readersAccess;

    private int amountWriters;

    private int amountReaders;
    
    private int waitingWritersLimit;

    public RWSemaphore(int waitingWritersLimit) {
        this.waitingWritersLimit = waitingWritersLimit;

        roomEmpty = new Semaphore(1);
        readersIncrement = new Semaphore(1);
        writersIncrement = new Semaphore(1);
        readersAccess = new Semaphore(1);

        amountWriters = 0;
        amountReaders = 0;
    }

    public void enterReader() {
        try {
            readersAccess.acquire();
            while(amountWriters > waitingWritersLimit)
                wait();
            readersAccess.release();

            readersIncrement.acquire();
            amountReaders++;
            readersIncrement.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enterWriter() {
        try {
            writersIncrement.acquire();
            amountWriters++;
            writersIncrement.release();

            roomEmpty.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void leaveReader() {
        try {
            readersIncrement.acquire();
            amountReaders--;
            readersIncrement.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void leaveWriter() {
        try {
            writersIncrement.acquire();
            amountWriters--;
            writersIncrement.release();

            roomEmpty.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
