package Allocator;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Debugger.Logger;

public class MTAllocator implements Allocator {
    public HashMap<Long, STAllocator> allocators;;

    private Logger logger;

    private ReadWriteLock lock;

    public MTAllocator() {
        allocators = new HashMap<>();
        logger = Logger.getInstance();
        lock = new ReentrantReadWriteLock();
    }

    /**
     * @return
     * 
     * Returns the single-threaded allocator pf the current thread
     */

    public STAllocator getAllocator(boolean createIfNotExists) {
        Long threadId = Thread.currentThread().getId();

        lock.readLock().lock();
        STAllocator allocator = allocators.get(threadId);
        lock.readLock().unlock();

        if(createIfNotExists && allocator == null) {
            allocator = new STAllocator();

            lock.writeLock().lock();
            allocators.put(threadId, allocator);
            lock.writeLock().unlock();
        }

        return allocator;
    }

    /**
     * @param size
     * @return
     * 
     * Allocates the given size in the thread's own allocator
     */

    @Override
    public Long allocate(int size) {
        Long address;
        STAllocator allocator = getAllocator(true);

        if(allocator == null)
            throw new NullPointerException();
            
        synchronized(allocator) {
            address = allocator.allocate(size);
        }

        return address;
    }

    /**
     * @param address
     * @throws AllocatorException
     * @return
     * 
     * Frees the given address
     */

    @Override
    public void free(Long address) throws AllocatorException {
        try {
            STAllocator allocator = getAllocator(false);

            synchronized(allocator) {
                allocator.free(address);
            }
        } catch(NullPointerException e) {
            boolean found = false;
            
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
        }
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) {
        free(oldAddress);
        return allocate(newSize);
    }

    @Override
    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }

    @Override
    public boolean isAccessible(Long address, int size) {
        lock.readLock().lock();

        for(STAllocator a : allocators.values()) {
            synchronized(a) {
                if(a.isAccessible(address, size)) {
                    lock.readLock().unlock();
                    return true;
                }
            }
        }

        lock.readLock().unlock();
        return false;
    }
}