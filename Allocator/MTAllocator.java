package Allocator;

import java.util.HashMap;

import Debugger.Logger;

public class MTAllocator implements Allocator {
    public static HashMap<Long, Allocator> allocators = new HashMap<>();

    private Logger logger;

    public MTAllocator() {
        logger = Logger.getInstance();
    }

    private Allocator getAllocator() {
        Long threadId = Thread.currentThread().getId();
        Allocator output = null;

        synchronized(allocators) {
            Allocator allocator = allocators.get(threadId);
            // logger.log(allocators);

            if(allocator == null) {
                allocator = new STAllocator();
                allocators.put(threadId, allocator);
            }
            
            // logger.log(allocators);

            output = allocator;
        }

        return output;
    }

    @Override
    public Long allocate(int size) {
        Allocator allocator = getAllocator();
        return allocator.allocate(size);
    }

    @Override
    public void free(Long address) throws AllocatorException {
        try {
            Allocator allocator = getAllocator();
            synchronized(allocator) {
                allocator.free(address);
            }
        } catch(NullPointerException e) {
            try {
                synchronized(allocators) {
                    for(Allocator a : allocators.values()) {
                        synchronized(a) {
                            a.free(address);
                        }
                    }
                }
            } catch(NullPointerException e2) {}
        }
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        try {
            Allocator allocator = getAllocator();
            synchronized(allocator) {
                allocator.reAllocate(oldAddress, newSize);
            }
        } catch(NullPointerException e) {
            try {
                synchronized(allocators) {
                    for(Allocator a : allocators.values()) {
                        try {
                            synchronized(a) {
                                return a.reAllocate(oldAddress, newSize);
                            }
                        } catch(AllocatorException e2) {}
                    }
                }
            } catch(NullPointerException e2) {}
        }

        return null;
    }

    @Override
    public boolean isAccessible(Long address) {
        boolean output = false;
        try {
            Allocator allocator = getAllocator();
            synchronized(allocator) {
                output = allocator.isAccessible(address);
            }

            if(output == false)
                throw new NullPointerException();
        } catch(NullPointerException e) {
            boolean b = false;
            synchronized(allocators) {
                for(Allocator a : allocators.values()) {
                    synchronized(a) {
                        b = a.isAccessible(address);
                    }
                    if(b) output = true;
                }
            }
        }
        return output;
    }

    @Override
    public boolean isAccessible(Long address, int size) {
        boolean output = false;
        try {
            Allocator allocator = getAllocator();
            synchronized(allocator) {
                synchronized(logger) {
                    output = allocator.isAccessible(address, size);
                }
            }
            if(output == false)
                throw new NullPointerException();  
        } catch(NullPointerException e) {
            boolean b = false;
            synchronized(allocators) {
                for(Allocator a : allocators.values()) {
                    synchronized(a) {
                        b = a.isAccessible(address, size);
                    }
                    if(b) output = true;
                }
            }
        }
        return output;
    }
}