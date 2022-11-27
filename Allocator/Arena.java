package Allocator;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import Debugger.Logger;

public class Arena {
    // list of blocks
    private ArrayList<Block> memoryBlocks;

    private BackingStore backingStore;

    // size of the blocks in de arena
    private int blockSize;

    // size of the pages in the blocks of the arena
    private int pageSize;

    private Semaphore mutex;

    private Logger logger;

    

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

        memoryBlocks = new ArrayList<>();
        backingStore = BackingStore.getInstance();
        mutex = new Semaphore(1);
        logger = Logger.getInstance();
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

        memoryBlocks = new ArrayList<>();
        backingStore = BackingStore.getInstance();
        mutex = new Semaphore(1);
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
        int i = 0;
        int size = 0;

        try {
            // mutex.acquire();
            size = memoryBlocks.size();

            for(i = 0; i < size; i++) {
                Block block = memoryBlocks.get(i);
                
                if(block.hasFreePages()) {
                    return block.getPage();
                }
            }

            memoryBlocks.add(new Block(backingStore.mmap(blockSize), pageSize, blockSize));
            // mutex.release();

            return getPage();
        // } catch(InterruptedException e) {
        //     e.printStackTrace();
        //     return null;
        } catch(IndexOutOfBoundsException e) {
            synchronized(logger) {
                logger.log(i + " : " + size);
                logger.log(e.getMessage());
            }

            return null;
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
        // try {
            // mutex.acquire();
            int size = memoryBlocks.size();
            // mutex.release();

            for(int i = 0; i < size; i++) {
                // mutex.acquire();
                Block block = memoryBlocks.get(i);
                // mutex.release();

                if(block.isAccessible(address)) {
                    try {
                        block.freePage(address);
                    } catch (EmptyBlockException e) {
                        // mutex.acquire();
                        memoryBlocks.remove(block);
                        // mutex.release();

                        backingStore.munmap(block.getStartAddress(), block.getBlockSize());
                    }
                    
                    return;
                }
            }
        // } catch (InterruptedException e) {
        //     logger.log(e.getMessage());
        // }

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
        
        // try {
            // mutex.acquire();
            int size = memoryBlocks.size();
            // mutex.release();

            for(int i = 0; i < size; i++) {
                // mutex.acquire();
                Block block = memoryBlocks.get(i);
                // mutex.release();
                
                if(block.isAccessible(address)) {
                    output = true;
                    break;
                }
            }

        // } catch (InterruptedException e) {
        //     logger.log(e.getMessage());
        // }

        return output;
    }
}


