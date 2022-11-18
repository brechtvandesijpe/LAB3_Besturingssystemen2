package Allocator;

import java.util.LinkedList;
import java.util.concurrent.*;

public class Arena {
    private final static int numReaders = 5;

    // list of blocks
    private LinkedList<Block> memoryBlocks;

    private BackingStore backingStore;

    private RWSemaphore blockAccess;

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
        memoryBlocks = new LinkedList<>();
        backingStore = BackingStore.getInstance();

        blockAccess = new RWSemaphore(numReaders);

        this.blockSize = blockSize;
        this.pageSize = blockSize;
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
        memoryBlocks = new LinkedList<>();
        backingStore = BackingStore.getInstance();
        
        blockAccess = new RWSemaphore(numReaders);

        this.blockSize = blockSize;
        this.pageSize = pageSize;
    }

    /**
     * 
     * @return
     * 
     * Method to get a free page from the arena.
     * 
     */

    public Long getPage() {
        blockAccess.enterReader();
        
        for(Block block : memoryBlocks){
            if(block.hasFreePages()) {
                try {
                    return block.getPage();
                } catch(AllocatorException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        blockAccess.leaveReader();

        blockAccess.enterWriter();
        memoryBlocks.add(new Block(backingStore.mmap(blockSize), pageSize, blockSize));
        blockAccess.leaveWriter();

        return getPage();
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
        blockAccess.enterReader();

        for(Block block : memoryBlocks){
            if(block.isAccessible(address)) {
                try {
                    block.freePage(address);
                } catch(EmptyBlockException e) {
                    blockAccess.enterWriter();
                    memoryBlocks.remove(block);
                    blockAccess.leaveWriter();

                    backingStore.munmap(block.getStartAddress(), block.getBlockSize());
                }

                return;
            }
        }

        blockAccess.leaveReader();

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
        blockAccess.enterReader();

        for(Block block : memoryBlocks){
            if(block.isAccessible(address))
                return true;
        }

        blockAccess.leaveReader();

        return false;
    }
}


