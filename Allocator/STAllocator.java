package Allocator;

import java.lang.Math;
import java.util.concurrent.ConcurrentHashMap;

import Debugger.Logger;

public class STAllocator implements Allocator {
    private ConcurrentHashMap<Integer, Arena> arenas;   // ConcurrentHashMap of arenas
    private Logger logger;                              // Logger to use

    /**
     * Constructor for the STAllocator.
     */

    public STAllocator() {
        arenas = new ConcurrentHashMap<>();
        this.logger = Logger.getInstance();
    }

    /**
     * @param number is the number to round
     * @return the rounded number
     * 
     * Method to get the next power of two following on a number.
     */

    private int baseTwo(int number) {
        return (int) (Math.pow(2, Math.ceil(Math.log(number) / Math.log(2))));
    }

    /**
     * @param number is the number to round
     * @return the rounded number
     * 
     * Method to get the next multiple of the minimal blocksize following on a number.
     */

    private int baseBlockSize(int number) {
        return (int) (Block.UNIT_BLOCK_SIZE * Math.ceil((double) number / (double) Block.UNIT_BLOCK_SIZE));
    }

    /**
     * @param size is the size to allocate
     * @throws AllocatorException when the size is illegal
     * @return the address of the allocation
     * 
     * Allocates a size of memory
     */

    public Long allocate(int size) throws AllocatorException {
        // If the size is illegal throw an exception
        if(size <= 0)
            throw new AllocatorException("Size can't be negative or zero");
        
        int roundedSizeBaseTwo = baseTwo(size);
        int roundedSizeBaseBlockSize = baseBlockSize(size);

        // Get the arena with the given size
        Arena arena = arenas.get(roundedSizeBaseTwo);

        if(arena == null)
            arena = arenas.get(roundedSizeBaseBlockSize);
        
        // If the arena doesn't exist, create it    
        if(arena == null) {
            if(size > Block.UNIT_BLOCK_SIZE) {
                arena = new Arena(roundedSizeBaseBlockSize);
                arenas.put(roundedSizeBaseBlockSize, arena);
            } else {
                arena = new Arena(4 * Block.UNIT_BLOCK_SIZE, roundedSizeBaseTwo);
                arenas.put(roundedSizeBaseTwo, arena);
            }

        }

        // Allocate a new block from the arena
        Long address = arena.allocate();
        return address;
    }

    /**
     * @param address is the address that has to be freed
     * @throws AllocatorException when the address is not allocated
     * @return nothing
     * 
     * Frees the arena where the address is present
     */

    public void free(Long address) throws AllocatorException {
        Arena arena = null;

        for(Arena a : arenas.values()) {
            if(a.isAccessible(address, 1)) {
                arena = a;
                try {
                    arena.free(address);
                } catch (ArenaException e) {}
                return;
            }
        }

        throw new AllocatorException("Address is not allocated");
    }

    /**
     * @param oldAddress is the old address of the allocation
     * @param newSize is the new size of the allocation
     * @throws AllocatorException when the size is illegal or the address is not allocated
     * @return the address of the new allocation
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
     * @param address is the address to check
     * @return if the address is allocated
     * 
     * Checks if the address is allocated
     */

    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }

    /**
     * @param address is the address to check
     * @param range is the range to check
     * @return if the address with given range is allocated
     * 
     * Checks if the address with given range is allocated
     */

    public boolean isAccessible(Long address, int range) {
        for(Arena arena : arenas.values()) {
            if(arena.isAccessible(address, range)) {
                return true;
            }
        }
        
        return false;
    }
}