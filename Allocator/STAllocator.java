package Allocator;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.LinkedList;
import Debugger.Logger;

public class STAllocator implements Allocator {
    private NavigableMap<Integer, Arena> arenas;

    private Logger logger;

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
        if(size <= 0)
            throw new AllocatorException("Size can't be negative or zero");
        
        Arena arena = arenas.get(size);
        
        if(arena == null) {
            if(size > 4096)
                arena = new Arena(Block.UNIT_BLOCK_SIZE);
            else
                arena = new Arena(Block.UNIT_BLOCK_SIZE, size);

            arenas.put(size, arena);
        }

        return arena.getPage();
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

        arena.freePage(address);
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
        if(newSize <= 0)
            throw new AllocatorException("Size can't be negative or zero");

        free(oldAddress);
        Long output = allocate(newSize);
        return output;
    }

    /**
     * @param address
     * @return
     * 
     * Checks if the address is allocated
     */

    public boolean isAccessible(Long address) {
        boolean output = getArena(address) != null;
        return output;
    }

    /**
     * @param address
     * @param size
     * @return
     * 
     * Checks if the address with given size is allocated
     */

    public boolean isAccessible(Long address, int size) {
        Arena arena = arenas.get(size);
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
        for(Arena arena : arenas.values()) {
            if(arena.isAccessible(address))
                return arena.getPageSize();
        }
        
        throw new IllegalArgumentException("Address is not allocated in this STAllocator");
    }
}