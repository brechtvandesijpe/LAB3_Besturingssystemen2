package Allocator;
<<<<<<< Updated upstream:Allocator/AllocatorImplementation.java

import java.util.*;

public class AllocatorImplementation implements Allocator {
    /* Modify this static var to return an instantiated version of your allocator  */
    private static Allocator instance = null;

    private HashMap<Integer, Arena> pageSizes;

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
            pageSizes.put(roundedSize, new Arena(roundedSize));
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
=======
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class STAllocator implements Allocator {
    private NavigableMap<Integer, Arena> alloccedBlocks = new TreeMap<>();

    private Arena getArena(Long address) {
        for(Arena arena : alloccedBlocks.values()) {
            boolean b = arena.isAccessible(address);
            if(b) return arena;
        }

        return null;
    }

    @Override
    public synchronized Long allocate(int size) {
        if(size <= 0)
            throw new AllocatorException("Size can't be negative or zero");
        
        Arena arena = alloccedBlocks.get(size);
        
        if(arena == null) {
            if(size > 4096)
                arena = new Arena(Block.UNIT_BLOCK_SIZE);
            else
                arena = new Arena(Block.UNIT_BLOCK_SIZE, size);
            alloccedBlocks.put(size, arena);
        }

        return arena.getPage();
>>>>>>> Stashed changes:Allocator/STAllocator.java
    }

    @Override
    public synchronized void free(Long address) throws AllocatorException {
        Arena arena = getArena(address);

        if(arena == null)
            throw new AllocatorException("Address is not allocated");

<<<<<<< Updated upstream:Allocator/AllocatorImplementation.java
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
=======
        arena.freePage(address);
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) throws AllocatorException {
        if(newSize <= 0)
            throw new AllocatorException("Size can't be negative or zero");

        free(oldAddress);
        return allocate(newSize);
    }
>>>>>>> Stashed changes:Allocator/STAllocator.java

    @Override
    public synchronized boolean isAccessible(Long address) {
        return getArena(address) != null;
    }

<<<<<<< Updated upstream:Allocator/AllocatorImplementation.java
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
=======
    @Override
    public synchronized boolean isAccessible(Long address, int size) {
        Arena arena = alloccedBlocks.get(size);
        if(arena == null) return false;
        else return arena.isAccessible(address);
>>>>>>> Stashed changes:Allocator/STAllocator.java
    }
}
