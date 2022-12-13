package Allocator;

import java.util.concurrent.ConcurrentLinkedQueue;
import Debugger.*;

public class Arena {
    private ConcurrentLinkedQueue<Block> memoryBlocks;  // ConcurrentLinkedQueue of memory blocks
    private BackingStore backingStore;                  // BackingStore to use
    private int blockSize;                              // Size of the blocks in the arena
    private int pageSize;                               // Size of the pages in the blocks in the arena
    private Logger logger;                              // Logger to use

    /**
     * @param blockSize is the size of the blocks in the arena
     * 
     * Constructor for an arena with an equal block and page size.
     */

    public Arena(int blockSize){
        this.blockSize = blockSize;
        this.pageSize = blockSize;

        memoryBlocks = new ConcurrentLinkedQueue<>();
        backingStore = BackingStore.getInstance();
        logger = Logger.getInstance();
    }

    /**
     * @param blockSize is the size of the blocks in the arena
     * @param pageSize is the size of the pages in the blocks of the arena
     * 
     * Constructor for an arena with a specific block and page size.
     */

    public Arena(int blockSize, int pageSize){
        this.blockSize = blockSize;
        this.pageSize = pageSize;

        memoryBlocks = new ConcurrentLinkedQueue<>();
        backingStore = BackingStore.getInstance();
        logger = Logger.getInstance();
    }

    /**
     * @return the size of the blocks in the arena
     * 
     * Get the nidividual size of the memory blocks in the arena.
     */

    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return the size of the pages in the blocks in the arena
     * 
     * Get the size of the pages in the memory blocks in the arena.
     */

    public int getPageSize() {
        return pageSize;
    }

    /**
     * @return the address of the allocation
     * 
     * Method to allocate a page in the arena
     */

    public Long allocate() {
        try {
            for(Block block : memoryBlocks){
                if(block.hasFreePages())
                    return block.allocate();
            }

            memoryBlocks.add(new Block(backingStore.mmap(blockSize), pageSize, blockSize));
            return allocate();
        } catch (BlockException e) {
            logger.log(e.getMessage());
            return null;
        }
    }

    /**
     * @param address is the address that has to be freed
     * @throws AllocatorException when the address is not present in the arena
     * 
     * Method to free an address from the arena.
     */

    public void free(Long address) throws ArenaException {
        for(Block block : memoryBlocks){
            if(block.isAccessible(address)) {
                    try {
                    block.free(address);
                    return;
                } catch(BlockException e) {
                    if(memoryBlocks.size() > 1) {
                        memoryBlocks.remove(block);
                        backingStore.munmap(block.getStartAddress(), block.getBlockSize());
                        return;
                    }
                }    
            }    
        }

        throw new ArenaException("Page not present in arena");
    }

    /**
     * @param address is the address to check
     * @return if the address is allocated
     * 
     * Method to check if the arena is accessible on a specific address.
     */

    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }

    /**
     * @param address is the address to check
     * @param range is the range after the address to check
     * @return if the address and ranges is allocated
     * 
     * Method to check if the arena is accessible on a specific address and a given range after that address.
     */

    public boolean isAccessible(Long address, int range) {
        for(Block block : memoryBlocks){
            if(block.isAccessible(address, range))
                return true;
        }

        return false;
    }
}