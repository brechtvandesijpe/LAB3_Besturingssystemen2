package Allocator;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.Semaphore;

import Debugger.Logger;

public class MTAllocator implements Allocator {
    public HashMap<String, STAllocator> allocators;;

    private Logger logger;

    private ReadWriteLock lock;

    public MTAllocator() {
        allocators = new HashMap<>();
        logger = Logger.getInstance();
        lock = new ReentrantReadWriteLock();
    }

    /**
     * @param createIfNotExists
     * @return
     * 
     * Returns the single-threaded allocator of the current thread, if createIfnotExists is true a new allocator can be created when the
     * current thread has no allocator yet
     */

    public STAllocator getAllocator(boolean createIfNotExists) {
        String threadName = Thread.currentThread().getName();

        lock.readLock().lock();
        STAllocator allocator = allocators.get(threadName);
        lock.readLock().unlock();

        if(allocator == null) {
            if(createIfNotExists) {
                allocator = new STAllocator();
    
                lock.writeLock().lock();
                allocators.put(threadName, allocator);
                lock.writeLock().unlock();
            } else throw new AllocatorException("Allocator does not exist");
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
            
        address = allocator.allocate(size);

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
        } catch(AllocatorException e) {
            boolean found = false;
            
            lock.readLock().lock();
            
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