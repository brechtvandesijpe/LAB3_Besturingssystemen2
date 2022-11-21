package Allocator;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class MTAllocator implements Allocator {
    public static HashMap<Long, Allocator> allocators = new HashMap<>();

    private Logger logger;

    private Semaphore mutex;

    public MTAllocator() {
        logger = Logger.getInstance();
        mutex = new Semaphore(1);
    }

    private Allocator getAllocator() {
        Long threadId = Thread.currentThread().getId();
        Allocator output = null;

        try {
            mutex.acquire();
            Allocator allocator = allocators.get(threadId);
            // logger.log(allocators);

            if(allocator == null) {
                allocator = new STAllocator();
                allocators.put(threadId, allocator);
            }
            
            // logger.log(allocators);

            output = allocator;
            mutex.release();
        } catch (InterruptedException e) {
            logger.log(e.getMessage());
        }

        return output;
    }

    @Override
    public Long allocate(int size) {
        return getAllocator().allocate(size);
    }

    @Override
    public void free(Long address) throws AllocatorException {
        getAllocator().free(address);
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        return getAllocator().reAllocate(oldAddress, newSize);
    }

    @Override
    public boolean isAccessible(Long address) {
        return getAllocator().isAccessible(address);
    }

    @Override
    public boolean isAccessible(Long address, int size) {
        return getAllocator().isAccessible(address, size);
    }
}