package Allocator;

import java.util.*;

public class AllocatorImplementation implements Allocator {
    /* Modify this static var to return an instantiated version of your allocator  */
    private static Allocator instance = null;

    private static BackingStore backingStore = null;

    private HashMap<Integer, Arena> pageSizes;

    /**
     * 
     * @param size
     * @return
     * 
     * Method to get the next biggest pagesize of a random given size.
     * 
     */

    private int roundUp(int size){
        return (int) Math.pow(2, Math.ceil(Math.log(size) / Math.log(2)));
    }

    private Allocator getInstance() {
        if (instance == null)
            instance = new AllocatorImplementation();
        return instance;
    }

    private AllocatorImplementation() {
        this.pageSizes = new HashMap<>();

        for(int i = 3; i < 12; i++) {
            int pageSize = (int) Math.pow(2, i);
            pageSizes.put(pageSize, new Arena(pageSize));
        }

        backingStore = BackingStore.getInstance();
    }

    /* Allocates a new region of memory with the specified size */
    public Long allocate(int size) {
        int roundedSize = roundUp(size);
        
        if(roundedSize <= 2048)
            pageSizes.get(roundedSize).getPage();
        else {
            pageSizes.put(roundedSize, new Arena(Block.BLOCK_SIZE, Math.ceil(roundedSize / Block.BLOCK_SIZE)));
        }
    }

    /**
     * 
     * @param address
     * @return
     * 
     * Releases the region of memory pointed to by `address`.
     * This memory can be reused to serve future `allocate` requests.
     */

    public void free(Long address) {

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
     */

    public Long reAllocate(Long oldAddress, int newSize) {

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
     */

    public boolean isAccessible(Long address, int size) {
        Arena arena = pageSizes.get(size);
    }
}