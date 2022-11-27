package Allocator;

import java.util.HashMap;

import Debugger.Logger;

public class MTAllocator implements Allocator {
    public static HashMap<Long, STAllocator> allocators = new HashMap<>();

    private Logger logger;

    public MTAllocator() {
        logger = Logger.getInstance();
    }

    private STAllocator getAllocator() {
        Long threadId = Thread.currentThread().getId();

        synchronized(allocators) {
            STAllocator allocator = allocators.get(threadId);
            
            if(allocator == null) {
                allocator = new STAllocator();
                allocators.put(threadId, allocator);
            }
            
            return allocator;
        }
    }

    @Override
    public Long allocate(int size) {
        STAllocator allocator = getAllocator();
        return allocator.allocate(size);
    }

    @Override
    public void free(Long address) throws AllocatorException {
        try {
            STAllocator allocator = getAllocator();
            allocator.free(address);
        } catch(NullPointerException e) {
            synchronized(allocators) {
                for(STAllocator a : allocators.values()) {
                    try {
                        a.free(address);
                    } catch(NullPointerException e2) {}
                }
            }
        }
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        try {
            STAllocator allocator = getAllocator();
            return allocator.reAllocate(oldAddress, newSize);
        } catch(NullPointerException npe) {
            synchronized(allocators) {
                for(STAllocator a : allocators.values()) {
                    try {
                        Long output = a.reAllocate(oldAddress, newSize);
                        if(output != null) 
                            return output;
                    } catch(AllocatorException ae) {}
                }
            }
        }
        return null;
    }

    @Override
    public boolean isAccessible(Long address) {
        try {
            STAllocator allocator = getAllocator();
            boolean output = allocator.isAccessible(address);

            if(!output)
                throw new NullPointerException();

            return true;
        } catch(NullPointerException e) {
            synchronized(allocators) {
                for(Allocator a : allocators.values()) {
                    if(a.isAccessible(address))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAccessible(Long address, int size) {
        try {
            STAllocator allocator = getAllocator();
            boolean output = allocator.isAccessible(address, size);

            if(!output)
                throw new NullPointerException();

            return true;
        } catch(NullPointerException e) {
            synchronized(allocators) {
                for(Allocator a : allocators.values()) {
                    if(a.isAccessible(address, size))
                        return true;
                }
            }
        }
        return false;
    }
}