package Allocator;

import java.util.*;

public class AllocatorImplementation implements Allocator {
    /* Modify this static var to return an instantiated version of your allocator  */
    private static Allocator instance = null;

    private HashMap<Integer, Arena> pageSizes;

    private RWSemaphore pageAccess;

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
        return (int) Math.pow(root, Math.ceil(Math.log(size) / Math.log(root)));
    }

    public static synchronized Allocator getInstance() {
        if (instance == null)
            instance = new AllocatorImplementation();
        return instance;
    }

    private AllocatorImplementation() {
        this.pageSizes = new HashMap<>();

        for(int i = 3; i < 12; i++) {
            int pageSize = (int) Math.pow(2, i);
            pageSizes.put(pageSize, new Arena(Block.UNIT_BLOCK_SIZE, pageSize));
        }    
        
        pageAccess = new RWSemaphore(5);
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
        int roundedSize = roundUp(size, 2);
        
        if(roundedSize <= Block.UNIT_BLOCK_SIZE / 2) {
            // pageAccess.enterReader();
            pageSizes.get(roundedSize).getPage();
            // pageAccess.leaveReader();
        } else {
            roundedSize = roundUp(size, Block.UNIT_BLOCK_SIZE);

            // pageAccess.enterWriter();
            pageSizes.put(roundedSize, new Arena(roundedSize));
            // pageAccess.leaveWriter();
        }

        // pageAccess.enterReader();
        Long output = pageSizes.get(roundedSize).getPage();
        // pageAccess.leaveReader();

        return output;
    }

    /**
     * 
     * @param address
     * 
     * Method to get the arena of a memory-address
     * 
     */

    private Arena getLocation(Long address) {
        Arena output = null;
        // pageAccess.enterReader();

        for(Arena arena : pageSizes.values()) {
            if(arena.isAccessible(address)) {
                output = arena;
                break;
            }
        }

        // pageAccess.leaveReader();
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
        Arena arena = getLocation(address);
        if(arena != null)
            arena.freePage(address);
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
        Long newAddress = allocate(newSize);
        free(oldAddress);
        return newAddress;
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
        boolean output = false;
        // pageAccess.enterReader();

        for(Integer entry : pageSizes.keySet()) {
            if(pageSizes.get(entry).isAccessible(address)) {
                output = true;
                break;
            }
        }

        // pageAccess.leaveReader();
        return output;
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
        // pageAccess.enterReader();
        Arena arena = pageSizes.get(size);
        // pageAccess.leaveReader();
        
        return arena.isAccessible(address);
    }
}