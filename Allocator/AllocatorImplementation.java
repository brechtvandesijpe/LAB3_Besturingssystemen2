package Allocator;

import java.util.*;
import java.util.concurrent.*;

public class AllocatorImplementation implements Allocator {
    /* Modify this static var to return an instantiated version of your allocator  */
    private static Allocator instance = null;

    private HashMap<Integer, Arena> pageSizes;

    private Semaphore pageLock;

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

    public static Allocator getInstance() {
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
        
        pageLock = new Semaphore(1);
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
        
        if(roundedSize <= Block.UNIT_BLOCK_SIZE / 2)
            pageSizes.get(roundedSize).getPage();
        else {
            roundedSize = roundUp(size, Block.UNIT_BLOCK_SIZE);

            try {
                pageLock.acquire();
                pageSizes.put(roundedSize, new Arena(roundedSize));
                pageLock.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return pageSizes.get(roundedSize).getPage();
    }

    /**
     * 
     * @param address
     * 
     * Method to get the arena of a memory-address
     * 
     */

    private Arena getLocation(Long address) {
        for(Arena arena : pageSizes.values()) {
            if(arena.isAccessible(address))
                return arena;
        }

        return null;
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
        for(Integer entry : pageSizes.keySet()) {
            if(pageSizes.get(entry).isAccessible(address))
                return true;
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
        Arena arena = pageSizes.get(size);
        return arena.isAccessible(address);
    }
}