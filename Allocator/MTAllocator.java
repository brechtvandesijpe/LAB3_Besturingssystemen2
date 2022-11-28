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

    private STAllocator getAllocator() {
        Long threadId = Thread.currentThread().getId();
        STAllocator allocator;

        synchronized(allocators) {
            allocator = allocators.get(threadId);
        }
            
        if(allocator == null) {
            allocator = new STAllocator();
            synchronized(allocators) {
                allocators.put(threadId, allocator);
            }
        }
            
        return allocator;
    }

    private LinkedList<STAllocator> getAllocators(STAllocator excludedAllocator) {
        LinkedList<STAllocator> output = new LinkedList<>();

        synchronized(allocators) {
            for(STAllocator a : allocators.values()) {
                if(a != excludedAllocator) output.add(a);
            }
        }

        return output;
    }

    @Override
    public Long allocate(int size) {
        STAllocator allocator = getAllocator();
        return allocator.allocate(size);
    }

    @Override
    public void free(Long address) throws AllocatorException {
        STAllocator allocator = null;

        try {
            allocator = getAllocator();
            allocator.free(address);
        } catch(AllocatorException ae) {
            for(STAllocator a : getAllocators(allocator)) {
                boolean b = false;

                try {
                    a.free(address);
                } catch(AllocatorException ae2) {
                    b = true;
                }
                
                if(!b)
                    break;
            }
        }
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        Long output = null;
        STAllocator allocator = null;

        try {
            allocator = getAllocator();
            return allocator.reAllocate(oldAddress, newSize);
        } catch(AllocatorException ae) {
            for(STAllocator a : getAllocators(allocator)) {
                try {
                    output = a.reAllocate(oldAddress, newSize);
                    if(output != null)
                        return output;
                } catch(AllocatorException ae2) {}   
            }
        }

        return output;
    }

    @Override
    public boolean isAccessible(Long address) {
        boolean output = false;
        STAllocator allocator = null;

        try {
            allocator = getAllocator();
            output = allocator.isAccessible(address);

            if(!output)
                throw new AllocatorException();
            
            return true;
        } catch(AllocatorException ae) {
            for(STAllocator a : getAllocators(allocator)) {
                try {
                    output = a.isAccessible(address);
                    if(output)
                        return output;
                } catch(AllocatorException ae2) {}   
            }
        }

        return output;
    }

    @Override
    public boolean isAccessible(Long address, int size) {
        boolean output = false;
        STAllocator allocator = null;

        try {
            allocator = getAllocator();
            
            output = allocator.isAccessible(address, size);

            if(!output)
                throw new AllocatorException();

            return true;
        } catch(AllocatorException ae) {
            for(STAllocator a : getAllocators(allocator)) {
                try {
                    output = a.isAccessible(address, size);
                    if(output)
                        return output;
                } catch(AllocatorException ae2) {}   
            }
        }

        return output;
    }
}