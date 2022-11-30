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
     * @param blockSize
     * 
     * Constructor for an arena with an equal block and page size.
     */

    public Arena(int blockSize){
        this.blockSize = blockSize;
        this.pageSize = blockSize;

        memoryBlocks = new LinkedList<>();
        backingStore = BackingStore.getInstance();
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
        for(Block block : memoryBlocks){
            if(block.hasFreePages())
                return block.allocate();
        }

        memoryBlocks.add(new Block(backingStore.mmap(blockSize), pageSize, blockSize));
        return allocate();
    }

    /**
     * @param address
     * @throws AllocatorException
     * 
     * Method to free an address from the arena.
     */

    public void free(Long address) throws AllocatorException {
        for(Block block : memoryBlocks){
            if(block.isAccessible(address)) {
                try {
                    block.free(address);
                } catch (EmptyBlockException e) {
                    memoryBlocks.remove(block);
                    backingStore.munmap(block.getStartAddress(), block.getBlockSize());
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
     * Method to check if a page is present in the arena.
     */

    public boolean isAccessible(Long address) {
        for(Block block : memoryBlocks){
            if(block.isAccessible(address))
                return true;
        }

        return false;
    }
}


