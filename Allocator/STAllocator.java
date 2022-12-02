package Allocator;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.lang.Math;

import Debugger.Logger;

public class STAllocator implements Allocator {
    private NavigableMap<Integer, Arena> arenas;        // map of arenas with their size as key
    private Logger logger;                              // For logging of events while debugging

    /**
     * Constructor for the STAllocator.
     */

    public STAllocator() {
        arenas = new TreeMap<>();
        this.logger = Logger.getInstance();
    }

    /**
     * @param number
     * @return int
     * 
     * Method to get the next power of two following on a number.
     */

    private int baseTwo(int number) {
        return (int) (Math.pow(2, Math.ceil(Math.log(number) / Math.log(2))));
    }

    /**
     * @param number
     * @return int
     * 
     * Method to get the next multiple of the minimal blocksize following on a number.
     */

    private int baseBlockSize(int number) {
        return (int) (Block.UNIT_BLOCK_SIZE * Math.ceil((double) number / (double) Block.UNIT_BLOCK_SIZE));
    }

    /**
     * @param size
     * @throws AllocatorException
     * @return Long
     * 
     * Allocates a size of memory
     */

    public Long allocate(int size) throws AllocatorException {
        // If the size is illegal throw an exception
        if(size <= 0)
            throw new AllocatorException("Size can't be negative or zero");
        
        // Calculate the pageSize in case the block is smaller than 4096 bytes (next power of 2)
        int roundedSizeBaseTwo = baseTwo(size);
        
        // Calculate the pageSize in case the block is bigger than 4096 bytes (next multiple of 4096)
        int roundedSizeBaseBlockSize = baseBlockSize(size);

        // Get the arena with the given size
        Arena arena = arenas.get(roundedSizeBaseTwo);

        if(arena == null)
            arena = arenas.get(roundedSizeBaseBlockSize);

        // If the arena doesn't exist, create it    
        if(arena == null) {
            if(size > Block.UNIT_BLOCK_SIZE) {
                // In case we give full blocks the pageSize equals the whole block
                arena = new Arena(roundedSizeBaseBlockSize);
                arenas.put(roundedSizeBaseBlockSize, arena);
            } else {
                arena = new Arena(Block.UNIT_BLOCK_SIZE, roundedSizeBaseTwo);
                arenas.put(roundedSizeBaseTwo, arena);
            }
        }

        // Allocate a new block from the arena
        Long address = arena.allocate();
        return address;
    }

    /**
     * @param address
     * @exception AllocatorException
     * @return void
     * 
     * Frees the arena where the address is present
     */

    public void free(Long address) throws AllocatorException {
        Arena arena = null;

        try {
            // Iterate over all the arena's and when the address is found, free it
            for(Arena a : arenas.values()) {
                if(a.isAccessible(address, 1)) {
                    arena = a;
                    arena.free(address);
                    return;
                }
            }
        } catch(ArenaException e) { // In case the arena throws an exception, the arena is ampty and thus shoudl be cleaned up
            arenas.remove(arena.getPageSize());
            return;
        }

        throw new AllocatorException("Address is not allocated");
    }

    /**
     * @param oldAddress
     * @param newSize
     * @throws AllocatorException
     * @return Long
     * 
     * Reallocates an old allocation to a new size
     */

    public Long reAllocate(Long oldAddress, int newSize) throws AllocatorException {
        // Check if the new size is valid
        if(newSize <= 0)
            throw new AllocatorException("Size can't be negative or zero");

        Arena arena = null;

        // Get the arena where the address is present
        for(Arena a : arenas.values()) {
            if(a.isAccessible(oldAddress, 1)) {
                arena = a;
                break;
            }
        }

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
     * @return boolean
     * 
     * Checks if the address is allocated
     */

    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }

    /**
     * @param address
     * @param range
     * @return boolean
     * 
     * Checks if the address with given range is allocated
     */

    public boolean isAccessible(Long address, int range) {
        // Iterate over all the arenas and check if the address is accessible, if so return true
        for(Arena arena : arenas.values()) {
            if(arena.isAccessible(address, range)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * @return String
     * 
     * Visualizes the current state of allocator
     */

    @Override
    public String toString() {
        return arenas + "";
    }
}