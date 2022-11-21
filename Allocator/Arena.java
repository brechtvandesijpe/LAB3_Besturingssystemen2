package Allocator;

import java.util.LinkedList;

public class Arena {
    // list of blocks
    private LinkedList<Block> memoryBlocks;

    private BackingStore backingStore;

    // size of the blocks in de arena
    private int blockSize;

    // size of the pages in the blocks of the arena
    private int pageSize;

    /**
     * 
     * @param blockSize
     * 
     * Constructor for an arena with an equal block and page size.
     * 
     */

    public Arena(int blockSize){
        this.blockSize = blockSize;
        this.pageSize = blockSize;

        memoryBlocks = new LinkedList<>();
        backingStore = BackingStore.getInstance();
    }

    /**
     * 
     * @param blockSize
     * @param pageSize
     * 
     * Constructor for an arena with a specific block and page size.
     * 
     */

    public Arena(int blockSize, int pageSize){
        this.blockSize = blockSize;
        this.pageSize = pageSize;

        memoryBlocks = new LinkedList<>();
        backingStore = BackingStore.getInstance();
    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * 
     * @return
     * 
     * Method to get a free page from the arena.
     * 
     */

    public Long getPage() {
        synchronized(memoryBlocks) {
            for(Block block : memoryBlocks){
                synchronized(block) {
                    if(block.hasFreePages()) {
                        return block.getPage();
                    }
                }
            }
            memoryBlocks.add(new Block(backingStore.mmap(blockSize), pageSize, blockSize));
            return getPage();
        }
    }

    /**
     * 
     * @param address
     * @throws AllocatorException
     * 
     * Method to free a page from the arena.
     * 
     */

    public void freePage(Long address) throws AllocatorException {
        synchronized(memoryBlocks) {
            for(Block block : memoryBlocks){
                if(block.isAccessible(address)) {
                    try {
                        block.freePage(address);
                    } catch (EmptyBlockException e) {
                        memoryBlocks.remove(block);
                        backingStore.munmap(block.getStartAddress(), block.getBlockSize());
                    }
                    
                    return;
                }
            }
        }

        throw new AllocatorException("Page not present in arena");
    }

    /**
     * 
     * @param address
     * @return
     * 
     * Method to check if a page is present in the arena.
     * 
     */

    public boolean isAccessible(Long address) {
        boolean output = false;
        synchronized(memoryBlocks) {
            for(Block block : memoryBlocks){
                if(block.isAccessible(address)) {
                    output = true;
                    break;
                }
            }
        }
        return output;
    }
}


