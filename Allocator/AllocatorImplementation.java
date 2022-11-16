package Allocator;

import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class AllocatorImplementation implements Allocator {
    /* Modify this static var to return an instantiated version of your allocator  */
    private static Allocator instance = new MyAllocatorImpl();

    private NavigableMap<Long, Long> alloccedBlocks = new TreeMap<>();

    private NavigableMap<Long, Long> allocatedPages = new TreeMap<>();
    private PriorityQueue<Page> pages = new PriorityQueue();
    /* Allocates a new region of memory with the specified size */
    public Long allocate(int size) {
        Long address = BackingStore.getInstance().mmap(size);
        Long sizeLong = (long) size;
        alloccedBlocks.put(address, sizeLong);
        System.out.println("Allocating " + size + " bytes at " + address);
        return address;
    }

    /* 
     * Releases the region of memory pointed to by `address`.
     * This memory can be reused to serve future `allocate` requests.
     */
    public void free(Long address) {
        Long size = alloccedBlocks.get(address);
        if (size == null)
            throw new AllocatorException("huh??");
        alloccedBlocks.remove(address);
        BackingStore.getInstance().munmap(address, size);
        System.out.println("Freeing " + size + " bytes at " + address);
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
//        free(oldAddress);
//        return allocate(newSize);

        // Increase Size -> Allocate the difference
        if(newSize - alloccedBlocks.get(oldAddress) > 0){
            allocate(Math.toIntExact(newSize - alloccedBlocks.get(oldAddress)));

        // Decrease Size -> Free the difference
        }else{
            free(oldAddress + alloccedBlocks.get(oldAddress) - newSize);
        }
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

    }

    /*
     * Same as above, except it allows to check a range of 
     * addresses more efficiently.
     * 
     * In addition, this method should verify that all addresses 
     * in the range belong to the same block of memory. 
     */
    public boolean isAccessible(Long address, int size) {

    }
}