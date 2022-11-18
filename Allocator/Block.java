package Allocator;

import java.util.BitSet;
import java.util.concurrent.*;

public class Block {
    public static final int UNIT_BLOCK_SIZE = 4096;

    private final Long startAddress;
    private final int pageSize;
    private final int blockSize;

    private BitSet allocatedPages;

    private Semaphore pageAccess;

    /**
     * 
     * @param startAddress
     * @param pageSize
     * 
     * COnstructor for a block.
     * 
     */

    public Block(Long startAddress, int pageSize, int blockSize) {
        this.startAddress = startAddress;
        this.pageSize = pageSize;
        this.blockSize = blockSize;

        allocatedPages = new BitSet();
        pageAccess = new Semaphore(1);
    }

    /**
     * 
     * @return
     * 
     * Get start address of the block.
     * 
     */

    public Long getStartAddress() {
        return startAddress;
    }

    /**
     * 
     * @return
     * 
     * Get the size of the block.
     * 
     */

    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return
     * @throws AllocatorException
     * 
     * Method to get a free page from the block.
     *
     */

    public Long getPage() throws AllocatorException {
        Long output = null;

        for(int i = 0; i < blockSize; i += pageSize){
            int pageIndex = i / pageSize;
            
            try {
                pageAccess.acquire();
                
                if(!allocatedPages.get(pageIndex)) {
                    allocatedPages.set(pageIndex);
                    output = startAddress + i;
                    break;
                }

                pageAccess.release();
            } catch(InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        if(output != null)
            return output;

        throw new AllocatorException("No free pages in block");
    }

    /**
     * @param address
     * @throws AllocatorException
     * 
     * Method to free a page from the block.
     * 
     */

    public void freePage(Long address) throws AllocatorException, EmptyBlockException {
        Long relativeAddress = address - startAddress;
        boolean empty = false;

        if(relativeAddress < 0)
            throw new AllocatorException("Page not present in block");
        
        int pageIndex = (int) Math.floor(relativeAddress / pageSize);

        try {
            pageAccess.acquire();
            allocatedPages.set(pageIndex, false);
            
            if(allocatedPages.isEmpty())
                empty = true;
            
            pageAccess.release();
        } catch(InterruptedException e) {
            System.out.println(e.getMessage());
        }

        if(empty)
            throw new EmptyBlockException("Block is empty");
    }

    /**
     * @param address
     * @return
     * 
     * Method to check if the block has free pages.
     * 
     */

    public boolean hasFreePages() {
        boolean output = false;
        
        try {
            pageAccess.acquire();
            
            for(int i = 0; i < blockSize / pageSize; i++) {
                if(!allocatedPages.get(i)) {
                    output = true;
                    break;
                }
            }
            
            pageAccess.release();
        } catch(InterruptedException e) {
            System.out.println(e.getMessage());
        }
        
        return output;
    }

    /**
     * 
     * @param address
     * @return
     * 
     * Method to check if the block is accessible on a specific address.
     * 
     */

    public boolean isAccessible(Long address) {
        Long relativeAddress = address - startAddress;
        boolean output = false;

        if(relativeAddress > 0) {    
            int pageIndex = (int) Math.floor(relativeAddress / pageSize);
            
            try {
                pageAccess.acquire();
                output =  allocatedPages.get(pageIndex);
                pageAccess.release();
            } catch(InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        
        return output;
    }
}