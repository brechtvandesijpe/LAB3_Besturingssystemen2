package Allocator;

import java.util.LinkedList;
import Debugger.*;

public class Arena {
    private LinkedList<Block> memoryBlocks;             // list of blocks in the arena

    private BackingStore backingStore;                  // backingstore for OS calls map and unmap

    private int blockSize;                              // size of the blocks in de arena
    private int pageSize;                               // size of the pages in the blocks of the arena

    private Logger logger;                              // logger for debugging

    /**
     * @param blockSize
     * 
     * Constructor for an arena with an equal block and page size.
     */

    public Arena(int blockSize){
        this.blockSize = blockSize;
        this.pageSize = blockSize;

        memoryBlocks = new LinkedList<>();
        backingStore = BackingStore.getInstance();
        logger = Logger.getInstance();
    }

    /**
     * @param blockSize
     * @param pageSize
     * 
     * Constructor for an arena with a specific block and page size.
     */

    public Arena(int blockSize, int pageSize){
        this.blockSize = blockSize;
        this.pageSize = pageSize;

        memoryBlocks = new LinkedList<>();
        backingStore = BackingStore.getInstance();
        logger = Logger.getInstance();
    }

    /**
     * @return int
     * 
     * Get the nidividual size of the memory blocks in the arena.
     */

    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return int
     * 
     * Get the size of the pages in the memory blocks in the arena.
     */

    public int getPageSize() {
        return pageSize;
    }

    /**
     * @return
     * 
     * Method to allocate a page in the arena
     */

    public Long allocate() {
        try {
            // Iterate over all the blocks in the arena and check if there are free pages within the blocks, if so allocate
            for(Block block : memoryBlocks){
                if(block.hasFreePages())
                    return block.allocate();
            }

            // If there are no free pages in te arena, map and create a new block and allocate a page from it
            Block b = new Block(backingStore.mmap(blockSize), pageSize, blockSize);
            memoryBlocks.add(b);
            return b.allocate();
        } catch (BlockException e) {
            // If thrown, an error has occured when creating the block
            logger.log(e.getMessage());
            return null;
        }
    }

    /**
     * @param address
     * @throws AllocatorException
     * 
     * Method to free an address from the arena.
     */

    public void free(Long address) throws AllocatorException, ArenaException {
        // Iterate over all the blocks and check if the address was allocated in the block, if so free the address
        for(Block block : memoryBlocks){
            if(block.isAccessible(address)) {
                try {
                    block.free(address);
                } catch (BlockException e) {
                    // If the block is empt after freeing: remove and unmap the block
                    memoryBlocks.remove(block);
                    backingStore.munmap(block.getStartAddress(), block.getBlockSize());
                    
                    // Check if the arena is empty, if so let the allocator know
                    if(memoryBlocks.isEmpty())
                        throw new ArenaException("No blocks in the arena");
                }

                return;
            }
        }

        throw new AllocatorException("Page not present in arena");
    }

    /**
     * @param address
     * @return
     * 
     * Method to check if the arena is accessible on a specific address.
     */

    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }

    /**
     * @param address
     * @return
     * 
     * Method to check if the arena is accessible on a specific address and a given range after that address.
     */

    public boolean isAccessible(Long address, int range) {
        // Iterate over all the blocks and check if the address is accessible in the block, if so return true
        for(Block block : memoryBlocks){
            if(block.isAccessible(address, range))
                return true;
        }

        return false;
    }

    /**
     * @return String
     * 
     * Method to visualize the arena.
     */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < memoryBlocks.size(); i++) {
            sb.append(memoryBlocks.get(i).toString());
        }

        return sb.toString();
    }
}