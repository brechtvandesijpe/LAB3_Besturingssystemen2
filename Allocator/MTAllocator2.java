package Allocator;

import java.util.concurrent.ConcurrentHashMap;

import Debugger.Logger;

public class MTAllocator2 implements Allocator {
    public ConcurrentHashMap<Long, STAllocator> allocators;           // Map of single-threaded allocators
    private Logger logger;                                            // Logger to use


    public MTAllocator2() {
        allocators = new ConcurrentHashMap<>();
        logger = Logger.getInstance();
    }

    /**
     * @param createIfNotExists is the flag to create a new allocator if the current thread has no allocator yet
     * @return the single-threaded allocator of the current thread
     * 
     * Returns the single-threaded allocator of the current thread, if createIfnotExists is true a new allocator can be created when the
     * current thread has no allocator yet
     */

    private STAllocator getAllocator(boolean createIfNotExists) {
        Long threadId = Thread.currentThread().getId();

        STAllocator allocator;

        // Use the computeIfAbsent method to check if the allocator exists
        // and create it if it does not
        allocator = allocators.computeIfAbsent(threadId, (k) -> {
            if(createIfNotExists) {
                return new STAllocator();
            } else {
                throw new AllocatorException("Allocator does not exist");
            }
        });

        return allocator;
    }

    /**
     * @param size is the size of the allocation
     * @return the address of the allocation
     * 
     * Allocates the given size (ALWAYS) in the thread's own allocator
     */

    @Override
    public Long allocate(int size) {
        Long address;
        STAllocator allocator = getAllocator(true);

        if (allocator == null)
            throw new NullPointerException();
        address = allocator.allocate(size);
        return address;
    }

    /**
     * @param address is the address to free
     * @return nothing
     * 
     * Frees the given address
     */

    @Override
    public void free(Long address) {
        boolean found = false;

        // Synchronize on the allocators map to control access
        synchronized (allocators) {
            // Iterate over the allocators map and call the free method
            // on the appropriate allocator
            for (STAllocator a : allocators.values()) {
                if (a.isAccessible(address)) {
                    a.free(address);
                    found = true;
                    break;
                }
            }
        }

        // If the address was not found in any allocator, log a warning
        if (!found) {
            logger.log("Address " + address + " not found in any allocator");
        }
    }

    /**
     * @param oldAddress is the old address of the allocation
     * @param newSize is the new size of the allocation
     * @return the address of the new allocation
     * 
     * Reallocates the given address to the given new size
     */

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        // Synchronize on the allocators map to control access
        synchronized (allocators) {
            // Iterate over the allocators map and call the free method
            // on the appropriate allocator
            for (STAllocator a : allocators.values()) {
                if (a.isAccessible(oldAddress)) {
                    a.free(oldAddress);
                    break;
                }
            }

            // Get the allocator of the current thread
            STAllocator allocator = getAllocator(true);
            if (allocator == null)
                throw new NullPointerException();

            // Allocate the new memory while synchronising on the allocator
            return allocator.allocate(newSize);
        }
    }

    /**
     * @param address is the address to check
     * @return if the address is allocated
     * 
     * Checks if the given address is accessible
     */

    @Override
    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }
    
    /**
     * @param address is the address to check
     * @param range is the range to check
     * @return if the address with the given range is allocated
     * 
     * Checks if the given address and range is accessible
     */

    @Override
    public boolean isAccessible(Long address, int size) {
        // Synchronize on the allocators map to control access
        synchronized (allocators) {
            // Iterate over the allocators map and check if the address is accessible
            // in any of the allocators
            for (STAllocator a : allocators.values()) {
                // Use a local variable to store the result of the isAccessible method
                boolean accessible = a.isAccessible(address);
                if (accessible) {
                    return true;
                }
            }
        }

        // If the address was not found in any allocator, return false
        return false;
    }
}