package Allocator;

import java.util.LinkedList;
import Debugger.*;

public class Arena {
    // list of blocks
    private LinkedList<Block> memoryBlocks;

    private BackingStore backingStore;

    // size of the blocks in de arena
    private int blockSize;

    // size of the pages in the blocks of the arena
    private int pageSize;

    private Logger logger;

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
     * @return
     * 
     * Get the nidividual size of the memory blocks in the arena.
     */

    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return
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
     * @param address
     * @throws AllocatorException
     * 
     * Method to free an address from the arena.
     */

    public void free(Long address) throws AllocatorException, ArenaException {
        for(Block block : memoryBlocks){
            if(block.isAccessible(address)) {
                try {
                    block.free(address);
                } catch (BlockException e) {
                    memoryBlocks.remove(block);
                    backingStore.munmap(block.getStartAddress(), block.getBlockSize());
                    
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
        for(Block block : memoryBlocks){
            if(block.isAccessible(address))
                return true;
        }

        return false;
    }

    /**
     * @param address
     * @return
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < memoryBlocks.size(); i++) {
            sb.append(memoryBlocks.get(i).toString());
        }

        return sb.toString();
    }
}


