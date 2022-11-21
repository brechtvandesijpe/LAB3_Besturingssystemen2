package Allocator;

import java.util.*;

public class STAllocator implements Allocator {
    /* Modify this static var to return an instantiated version of your allocator  */
    private HashMap<Integer, Arena> pageSizes;

    private Logger logger;

    /**
     * 
     * @param size
     * @param root
     * @return
     * 
     * Method to get the next biggest multiple of the root number
     * 
     */

    public static int roundUp(int size, int root){
        if (root == 0)
            return size;

        int remainder = size % root;
        if (remainder == 0)
            return size;

        return size + root - remainder;
    }

    public STAllocator() {
        this.pageSizes = new HashMap<>();

        for(int i = 3; i < 12; i++) {
            int pageSize = (int) Math.pow(2, i);
            pageSizes.put(pageSize, new Arena(Block.UNIT_BLOCK_SIZE, pageSize));
        }

        logger = Logger.getInstance();
        logger.log(pageSizes);
    }

    /**
     * 
     * @param size
     * @return
     * 
     * Allocates a new region of memory with the specified size
     * 
     */

    public Long allocate(int size) {
        logger.log(this +  " : malloc");
        Long output = 0L;
        int roundedSize = roundUp(size, 2);
        
        try {
            synchronized(pageSizes) {
                if(roundedSize <= Block.UNIT_BLOCK_SIZE / 2) {
                    Arena arena = pageSizes.get(roundedSize);
                    
                    synchronized(arena) {
                        arena.getPage();
                    }
                } else {
                    roundedSize = roundUp(size, Block.UNIT_BLOCK_SIZE);
                    pageSizes.put(roundedSize, new Arena(roundedSize));
                }

                output = pageSizes.get(roundedSize).getPage();
            }

            return output;
        } catch(NullPointerException e) {
            pageSizes.put(roundedSize, new Arena(Block.UNIT_BLOCK_SIZE, roundedSize));
            return allocate(size);
        }
    }

    /**
     * 
     * @param address
     * 
     * Method to get the arena of a memory-address
     * 
     */

    private Arena getLocation(Long address) {
        logger.log(this + " : location");
        Arena output = null;

        synchronized(pageSizes) {
            for(Arena arena : pageSizes.values()) {
                if(arena.isAccessible(address))
                    output = arena;
                    break;
            }
        }

        return output;
    }

    /**
     * 
     * @param address
     * @return
     * 
     * Releases the region of memory pointed to by `address`.
     * This memory can be reused to serve future `allocate` requests.
     * 
     */

    public void free(Long address) {
        logger.log(this +  " : free");
        try {
            Arena arena = getLocation(address);
            if(arena != null) {
                synchronized(arena) {
                    arena.freePage(address);
                }
            }
        } catch(NullPointerException e) {
            logger.log("Address: " + address + " nullpointer in free.");
        }
    }

    /**
     * 
     * @param oldAddress
     * @param newSize
     * @return
     * 
     * Releases the memory associated with `oldAddress` and immediately
     * returns a new region of memory with size `newSize`
     * 
     * This method is commonly supported by allocators because it
     * provides the possibility for optimization: when there is unused 
     * memory available next to the allocation, the original allocation
     * can be resized in-place, yielding performance gains.
     * 
     * It is allowed for this method to return new memory every time, 
     * but a more optimized implementation is encouraged!
     * 
     */

    public Long reAllocate(Long oldAddress, int newSize) {
        logger.log(this +  " : realloc");
        try {
            Arena arena = getLocation(oldAddress);
            
            int oldSize;

            synchronized(arena) {
                oldSize = arena.getPageSize();
            }

            if(oldSize >= newSize) {
                return oldAddress;
            } else {
                Long newAddress = allocate(newSize);
                free(oldAddress);
                return newAddress;
            }
        } catch(NullPointerException e) {
            logger.log("Address: " + oldAddress + " was not yet allocated.");
            return allocate(newSize);
        }
    }
    
    /**
     * 
     * @param address
     * @return
     * 
     * Returns `true` when `address` refers to a currently allocated block.
     * Returns `false` otherwise.
     * 
     * This method is used by us to test your implementation. 
     * You still have to implement it yourselves, specifically for your
     * type of allocator.
     * 
     */

    public boolean isAccessible(Long address) {
        logger.log(this + " : accessable");
        synchronized(pageSizes) {
            for(Integer entry : pageSizes.keySet()) {
                if(pageSizes.get(entry).isAccessible(address))
                    return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param address
     * @param size
     * @return
     * 
     * Same as above, except it allows to check a range of 
     * addresses more efficiently.
     * 
     * In addition, this method should verify that all addresses 
     * in the range belong to the same block of memory. 
     * 
     */

    public boolean isAccessible(Long address, int size) {
        logger.log(this + " : accessable_size");
        synchronized(pageSizes) {
            Arena arena = pageSizes.get(size);
            return arena.isAccessible(address);
        }
    }
}