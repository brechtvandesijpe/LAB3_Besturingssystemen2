package Allocator;

import java.util.LinkedList;

public class Arena {
    // First fitting size, list of blocks
    private LinkedList<Block> memoryBlocks;

    private BackingStore backingStore;

    private static int blockSize;

    public Arena(int blockSize){
        // TODO correcte implementatie
        memoryBlocks = new LinkedList<>();
        backingStore = BackingStore.getInstance();
        Arena.blockSize = blockSize;

        if(blockSize > Block.BLOCK_SIZE) {
            Long rootAddress = backingStore.mmap(blockSize);
            Block b = new Block(rootAddress, Block.BLOCK_SIZE);
            memoryBlocks.add(b);

            for(int i = 0; i < blockSize / Block.BLOCK_SIZE; i++) {
                Long startAddress = backingStore.munmap();
                Block b = new Block(rootAddress, Block.BLOCK_SIZE);
            }

        }
    }

    /**
     * 
     * @return
     * 
     * Method to get a free page from the arena.
     * 
     */

    public Long getPage() {
        for(Block block : memoryBlocks){
            if(block.hasFreePages())
                return block.getPage();
        }

        memoryBlocks.add(new Block(backingStore.mmap(Block.BLOCK_SIZE), blockSize));
        return getPage();
    }

    /**
     * 
     * @param address
     * 
     * Method to free a page from the arena.
     * 
     */

    public void freePage(Long address) {
        for(Block block : memoryBlocks){
            if(block.isAccessible(address)){
                block.freePage(address);
                return;
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
        for(Block block : memoryBlocks){
            if(block.isAccessible(address))
                return true;
        }

        return false;
    }
}


