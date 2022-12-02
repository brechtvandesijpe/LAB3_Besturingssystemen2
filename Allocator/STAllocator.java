package Allocator;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.lang.Math;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Debugger.Logger;

public class STAllocator implements Allocator {
    private NavigableMap<Integer, Arena> arenas;

    private Logger logger;

    private ReadWriteLock lock;

    public STAllocator() {
        arenas = new TreeMap<>();
        this.logger = Logger.getInstance();
        lock = new ReentrantReadWriteLock();
    }

    private int baseTwo(int number) {
        return (int) (Math.pow(2, Math.ceil(Math.log(number) / Math.log(2))));
    }

    private int baseBlockSize(int number) {
        return (int) (Block.UNIT_BLOCK_SIZE * Math.ceil((double) number / (double) Block.UNIT_BLOCK_SIZE));
    }

    /**
     * @param size
     * @throws AllocatorException
     * @return
     * 
     * Allocates a new arena of the given size
     */

    public Long allocate(int size) throws AllocatorException {
        // If the size is illegal throw an exception
        if(size <= 0)
            throw new AllocatorException("Size can't be negative or zero");
        
        int roundedSizeBaseTwo = baseTwo(size);
        int roundedSizeBaseBlockSize = baseBlockSize(size);

        // Get the arena with the given size
        lock.readLock().lock();
        Arena arena = arenas.get(roundedSizeBaseTwo);

        if(arena == null)
            arena = arenas.get(roundedSizeBaseBlockSize);
        
        lock.readLock().unlock();

        // If the arena doesn't exist, create it    
        if(arena == null) {
            lock.writeLock().lock();
            if(size > Block.UNIT_BLOCK_SIZE) {
                arena = new Arena(roundedSizeBaseBlockSize);
                arenas.put(roundedSizeBaseBlockSize, arena);
            } else {
                arena = new Arena(Block.UNIT_BLOCK_SIZE, roundedSizeBaseTwo);
                arenas.put(roundedSizeBaseTwo, arena);
            }
            lock.writeLock().unlock();
        }

        // Allocate a new block from the arena
        Long address = arena.allocate();
        return address;
    }

    /**
     * @param address
     * @exception AllocatorException
     * @return
     * 
     * Frees the arena where the address is present
     */

    public void free(Long address) throws AllocatorException {
        Arena arena = null;

        try {
            lock.readLock().lock();
            for(Arena a : arenas.values()) {
                if(a.isAccessible(address, 1)) {
                    arena = a;
                    arena.free(address);
                    lock.readLock().unlock();
                    return;
                }
            }
            lock.readLock().unlock();
        } catch(ArenaException e) {
            lock.readLock().unlock();

            lock.writeLock().lock();
            arenas.remove(arena.getPageSize());
            lock.writeLock().unlock();
            return;
        }

        throw new AllocatorException("Address is not allocated");
    }

    /**
     * @param oldAddress
     * @param newSize
     * @throws AllocatorException
     * @return
     * 
     * Reallocates an old allocation to a new size
     */

    public Long reAllocate(Long oldAddress, int newSize) throws AllocatorException {
        // Check if the new size is valid
        if(newSize <= 0)
            throw new AllocatorException("Size can't be negative or zero");

        Arena arena = null;

        // Get the arena where the address is present
        lock.readLock().lock();
        for(Arena a : arenas.values()) {
            if(a.isAccessible(oldAddress, 1)) {
                arena = a;
                lock.readLock().unlock();
                break;
            }
        }
        lock.readLock().unlock();

        // If the arena is null, the address is not allocated and thus cannot be reallocated
        if(arena == null)
            throw new AllocatorException("Address is not allocated");

        // Get the old size of the allocation
        int oldSize = arena.getPageSize();
        
        // If the old size is greater or the same as the new size, return the old address
        if(oldSize >= newSize)
            return oldAddress;

        // Free the old address
        free(oldAddress);

        // Allocate a new address with the new size and return it
        return allocate(newSize);
    }

    /**
     * @param address
     * @return
     * 
     * Checks if the address is allocated
     */

    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }

    /**
     * @param address
     * @param size
     * @return
     * 
     * Checks if the address with given size is allocated
     */

    public boolean isAccessible(Long address, int size) {
        lock.readLock().lock();
        for(Arena arena : arenas.values()) {
            if(arena.isAccessible(address, size)) {
                lock.readLock().unlock();
                return true;
            }
        }
        lock.readLock().unlock();
        
        return false;
    }

    @Override
    public String toString() {
        // StringBuilder sb = new StringBuilder();

        // for(Arena arena : arenas.values())
        //     sb.append(arena.toString() + " ");

        // return sb.toString();
        return arenas + "";
    }
}