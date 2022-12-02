package Allocator;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Debugger.Logger;

public class MTAllocator implements Allocator {
    public HashMap<String, STAllocator> allocators;;

    private Logger logger;  // Logger for debugging

    private ReadWriteLock lock; // Lock to protect the map of allocators

    public MTAllocator() {
        allocators = new HashMap<>();
        logger = Logger.getInstance();
        lock = new ReentrantReadWriteLock();
    }

    /**
     * @param createIfNotExists
     * @return STAllocator
     * 
     * Returns the single-threaded allocator of the current thread, if createIfnotExists is true a new allocator can be created when the
     * current thread has no allocator yet
     */

    public STAllocator getAllocator(boolean createIfNotExists) {
        // Get the name of the current thread, just for testing purpose, normaly the thread id would be used
        String threadName = Thread.currentThread().getName();

        lock.readLock().lock();
        STAllocator allocator = allocators.get(threadName);
        lock.readLock().unlock();

        if(allocator == null) {
            if(createIfNotExists) { // If the allocator does not exist yet, create it and add it if allowed
                allocator = new STAllocator();
    
                lock.writeLock().lock();
                allocators.put(threadName, allocator);
                lock.writeLock().unlock();
            } else throw new AllocatorException("Allocator does not exist"); // If the allocator doesn't exist throw an exception
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
        STAllocator allocator = getAllocator(true); // Get the allocator of the current thread, if it doesn't exist create it

        if(allocator == null)
            throw new NullPointerException();   // If still something went wrong and the allocator is null, throw an exception
        
        synchronized(allocator) {
            address = allocator.allocate(size); // Allocate the given size in the allocator while synchronising on the allocator
        }

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
    public void free(Long address) throws AllocatorException {
        try {
            // Get the allocator of the thread that allocated the address
            STAllocator allocator = getAllocator(false);

            synchronized(allocator) {
                allocator.free(address);    // Free the address while synchronising on the allocator
            }
        } catch(AllocatorException e) {
            boolean found = false;
            
            lock.readLock().lock();
            
            // If not found in the own allocator search in all other allocators and try to free the address
            for(STAllocator allocator : allocators.values()) {
                try {
                    found = true;
                    synchronized(allocator) {
                        allocator.free(address);
                    }
                } catch(AllocatorException e2) {
                    found = false;
                }

                if(found)
                    break;
            }

            lock.readLock().unlock();
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
        free(oldAddress);
        return allocate(newSize);
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
    public boolean isAccessible(Long address, int range) {
        lock.readLock().lock();

        // Iterate over all alocators to check if the address is accessible, if so unlock the readlock and return true
        for(STAllocator a : allocators.values()) {
            synchronized(a) {
                if(a.isAccessible(address, range)) {
                    lock.readLock().unlock();
                    return true;
                }
            }
        }

        // Address not found within the allocator so unlock the readlock and return false
        lock.readLock().unlock();
        return false;
    }
}