package Allocator;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.LinkedList;
import java.lang.Math;

import Debugger.Logger;

public class STAllocator implements Allocator {
    private NavigableMap<Integer, Arena> arenas;

    private Logger logger;

    public static int round(int root, int multiple) {
        return (Math.round(root / multiple) * multiple);
    }

    public STAllocator() {
        arenas = new TreeMap<>();
        this.logger = Logger.getInstance();;
    }

    /**
     * @param address
     * @return
     * 
     * Gets the arena where the address is present
     */

    private Arena getArena(Long address) {
        for(Arena arena : arenas.values()) {
            boolean b = arena.isAccessible(address);
            if(b) return arena;
        }

        return null;
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
        
        int roundedSize = (int) (Math.pow(2, Math.ceil(Math.log(size) / Math.log(2))));

        // Get the arena with the given size
        Arena arena = arenas.get(size);
        
        // If the arena doesn't exist, create it    
        if(arena == null) {
            if(size > Block.UNIT_BLOCK_SIZE)
                arena = new Arena(roundedSize);
            else
                arena = new Arena(Block.UNIT_BLOCK_SIZE, roundedSize);

            arenas.put(size, arena);
        }

        // Allocate a new block from the arena
        return arena.allocate();
    }

    /**
     * @param address
     * @exception AllocatorException
     * @return
     * 
     * Frees the arena where the address is present
     */

    public void free(Long address) throws AllocatorException {
        Arena arena = getArena(address);

        if(arena == null)
            throw new AllocatorException("Address is not allocated");

        arena.free(address);
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

        // Get the arena where the address is present
        Arena arena = getArena(oldAddress);

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
        // If the arena is null, the address is not allocated
        return getArena(address) != null;
    }

    /**
     * @param address
     * @param size
     * @return
     * 
     * Checks if the address with given size is allocated
     */

    public boolean isAccessible(Long address, int size) {
        // Get the arena with the given size
        Arena arena = arenas.get(size);

        // If the arena is null, the address is defenetly not allocated, else check if the address is accessible within the arena
        if(arena == null)
            return false;
        else
            return arena.isAccessible(address);
    }

    /**
     * @param address
     * @return
     * 
     * Gets the size of the allocation where the address is present
     */

    public int getSize(Long address) {
        // Check all the arenas
        for(Arena arena : arenas.values()) {
            if(arena.isAccessible(address))
                return arena.getPageSize();
        }
        
        throw new IllegalArgumentException("Address is not allocated in this STAllocator");
    }
}