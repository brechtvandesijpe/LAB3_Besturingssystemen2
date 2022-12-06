package Allocator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Debugger.Logger;

public class MTAllocator implements Allocator {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    private ConcurrentHashMap<String, STAllocator> allocators;

    private Logger logger;  // Logger for debugging

    public MTAllocator() {
        allocators = new ConcurrentHashMap<>();
        logger = Logger.getInstance();
    }

    /**
     * @param createIfNotExists
     * @return STAllocator
     * 
     * Returns the single-threaded allocator of the current thread, if createIfnotExists is true a new allocator can be created when the
     * current thread has no allocator yet
     */

    public STAllocator getAllocator(boolean createIfNotExists) {
        String threadName = Thread.currentThread().getName();

        STAllocator allocator;

        // Acquire a read lock before accessing the allocators map
        rwLock.readLock().lock();
        try {
            allocator = allocators.get(threadName);
        } finally {
            // Release the read lock
            rwLock.readLock().unlock();
        }

        if(allocator == null) {
            if(createIfNotExists) {
                allocator = new STAllocator();
                // Acquire a write lock before modifying the allocators map
                rwLock.writeLock().lock();
                try {
                    allocators.put(threadName, allocator);
                } finally {
                    // Release the write lock
                    rwLock.writeLock().unlock();
                }
            } else throw new AllocatorException("Allocator does not exist");
        }

        return allocator;
    }

    /**
     * @param size
     * @return Long
     * 
     * Allocates the given size (ALWAYS) in the thread's own allocator
     */

    @Override
    public Long allocate(int size) {
        Long address;
        STAllocator allocator = getAllocator(true);

        if(allocator == null)
            throw new NullPointerException();
        address = allocator.allocate(size);
        return address;
    }

    /**
     * @param address
     * @throws AllocatorException
     * @return void
     * 
     * Frees the given address
     */

    @Override
    public void free(Long address) {
        boolean found = false;

        // Acquire a read lock on the allocators map
        rwLock.readLock().lock();
        try {
            // Iterate over the allocators map and call the free method
            // on the appropriate allocator
            for(STAllocator a : allocators.values()) {
                if(a.isAccessible(address)) {
                    a.free(address);
                    found = true;
                    break;
                }
            }
        } finally {
            // Release the read lock on the allocators map
            rwLock.readLock().unlock();
        }

        // If the address was not found in any allocator, log a warning
        if(!found) {
            logger.log("Address " + address + " not found in any allocator");
        }
    }

    /**
     * @param oldAddress
     * @param newSize
     * @return Long
     * 
     * Reallocates the given address to the given new size
     */

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        // Acquire a write lock on the allocators map
        rwLock.writeLock().lock();
        try {
            // Iterate over the allocators map and call the free method
            // on the appropriate allocator
            for(STAllocator a : allocators.values()) {
                if(a.isAccessible(oldAddress)) {
                    a.free(oldAddress);
                    break;
                }
            }

            // Get the allocator of the current thread
            STAllocator allocator = getAllocator(true);
            if(allocator == null)
                throw new NullPointerException();

            // Allocate the new memory while synchronising on the allocator
            return allocator.allocate(newSize);
        } finally {
            // Release the write lock on the allocators map
            rwLock.writeLock().unlock();
        }
    }

    /**
     * @param address
     * @return boolean
     * 
     * Checks if the given address is accessible
     */

    @Override
    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }

    /**
     * @param address
     * @param range
     * @return boolean
     * 
     * Checks if the given address and range is accessible
     */

    @Override
    public boolean isAccessible(Long address, int size) {
        // Acquire a read lock on the allocators map before iterating over it
        rwLock.readLock().lock();
        try {
            // Iterate over the allocators map and call the isAccessible method
            // on the appropriate allocator
            for(STAllocator a : allocators.values()) {
                if(a.isAccessible(address)) {
                    return a.isAccessible(address, size);
                }
            }
            return false;
        } finally {
            // Release the read lock on the allocators map
            rwLock.readLock().unlock();
        }
    }
}