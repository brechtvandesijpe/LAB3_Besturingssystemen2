package Allocator;

import java.util.Map;
import java.util.TreeMap;

public class AllocatorImplementation implements Allocator {
    /* Modify this static var to return an instantiated version of your allocator  */
    static Allocator instance = new MyAllocatorImpl();

    private static BackingStore backingStore = BackingStore.getInstance();

    private static Map<Long, Long> allocated_blocks = new TreeMap<>();    

    /* Allocates a new region of memory with the specified size */
    public Long allocate(int size) {
        Long allocated_block = backingStore.mmap(size);
        allocated_blocks.put(allocated_block, (long) size);
        return allocated_block;
    }

    /* 
     * Releases the region of memory pointed to by `address`.
     * This memory can be reused to serve future `allocate` requests.
     */
    public void free(Long address) {
        backingStore.munmap(address, allocated_blocks.get(address));
        allocated_blocks.remove(address);
    }

    /*
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
        allocated_blocks.remove(oldAddress);
        Long newAddress = backingStore.mmap(newSize);
        allocated_blocks.put(newAddress, (long) newSize);
        return newAddress;
    }
    
    /*
     * Returns `true` when `address` refers to a currently allocated block.
     * Returns `false` otherwise.
     * 
     * This method is used by us to test your implementation. 
     * You still have to implement it yourselves, specifically for your
     * type of allocator.
     */
    public boolean isAccessible(Long address) {
        for(Map.Entry<Long, Long> entry : allocated_blocks.entrySet()) {
            if(address >= entry.getKey() && address < entry.getKey() + entry.getValue())
                return true;
        }
        return false;
    }

    /*
     * Same as above, except it allows to check a range of 
     * addresses more efficiently.
     * 
     * In addition, this method should verify that all addresses 
     * in the range belong to the same block of memory. 
     */
    public boolean isAccessible(Long address, int size) {
        for(Map.Entry<Long, Long> entry : allocated_blocks.entrySet()) {
            if(address >= entry.getKey() && address + size < entry.getKey() + entry.getValue())
                return true;
        }
        return false;
    }
}