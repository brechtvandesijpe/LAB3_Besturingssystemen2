package Allocator;

import java.util.HashMap;
import java.util.LinkedList;

import Debugger.Logger;

public class MTAllocator implements Allocator {
    public HashMap<Long, STAllocator> allocators;;

    private Logger logger;

    public MTAllocator() {
        allocators = new HashMap<>();
        logger = Logger.getInstance();
    }

    /**
     * @param size
     * @return
     * 
     * Allocates the given size in the thread's own allocator
     */

    @Override
    public synchronized Long allocate(int size) {
        // Get the ThreadID
        Long threadId = Thread.currentThread().getId();

        // If the thread doesn't have an allocator, create one
        if (!allocators.containsKey(threadId))
            allocators.put(threadId, new STAllocator());

        // Use the allocator from the thread to allocate the size
        return allocators.get(threadId).allocate(size);
    }

    /**
     * @param address
     * @throws AllocatorException
     * @return
     * 
     * Frees the given address
     */

    @Override
    public synchronized void free(Long address) throws AllocatorException {
        try {
            // Try to free the address in the thread's own allocator
            Long threadId = Thread.currentThread().getId();

            // To free memory the allocator must exist
            if(!allocators.containsKey(threadId))
                throw new AllocatorException("Thread " + threadId + " has no allocator");

            // Check if the address is in the allocator, if so free it
            if(allocators.get(threadId).isAccessible(address))
                allocators.get(threadId).free(address);
            else
                throw new AllocatorException("Address " + address + " is not accessible");
        } catch(AllocatorException e) {
            // If the address is not present in the thread's own allocator, try to free it in the other allocators
            for(Long threadId : allocators.keySet()) {
                try {
                    allocators.get(threadId).free(address);
                    return;
                } catch(AllocatorException e2) {
                    // Do nothing
                }
            }
        }
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        free(oldAddress);
        return allocate(newSize);
    }

    @Override
    public synchronized boolean isAccessible(Long address) {
        for(Long threadId : allocators.keySet()) {
            try {
                if(allocators.get(threadId).isAccessible(address));
                    return true;
            } catch(AllocatorException e) {
                // Do nothing
            }
        }

        return false;
    }

    @Override
    public synchronized boolean isAccessible(Long address, int size) {
        for(Long threadId : allocators.keySet()) {
            try {
                if(allocators.get(threadId).isAccessible(address, size));
                    return true;
            } catch(AllocatorException e) {
                // Do nothing
            }
        }

        return false;
    }
}