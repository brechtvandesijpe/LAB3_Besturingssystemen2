package Allocator;

import java.util.HashMap;

import Debugger.Logger;

public class MTAllocator implements Allocator {
    public HashMap<Long, STAllocator> allocators;;

    private Logger logger;

    public MTAllocator() {
        allocators = new HashMap<>();
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
        } catch(AllocatorException ae) {
            // logger.log(address + " not found, iterating throug...");
            synchronized(allocators) {
                for(STAllocator a : allocators.values()) {
                    try {
                        a.free(address);
                    } catch(AllocatorException ae2) {}
                }
            }
        }
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        try {
            STAllocator allocator = getAllocator();
            return allocator.reAllocate(oldAddress, newSize);
        } catch(AllocatorException ae) {
            synchronized(allocators) {
                for(STAllocator a : allocators.values()) {
                    try {
                        Long output = a.reAllocate(oldAddress, newSize);
                        if(output != null) 
                            return output;
                    } catch(AllocatorException ae2) {}
                }
            }
        }
        return null;
    }

    @Override
    public boolean isAccessible(Long address) {
        try {
            boolean output;
            STAllocator allocator = getAllocator();

            output = allocator.isAccessible(address);

            if(!output)
                throw new AllocatorException();
            
            return true;
        } catch(AllocatorException ae) {
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
            boolean output;
            
            output = allocator.isAccessible(address, size);

            if(!output)
                throw new NullPointerException();

            return true;
        } catch(NullPointerException e) {
            synchronized(allocators) {
                for(Allocator a : allocators.values()) {
                    synchronized(a) {
                        if(a.isAccessible(address, size))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}