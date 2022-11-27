package Allocator;

import java.util.BitSet;

public class Block {
    public static final int UNIT_BLOCK_SIZE = 4096;

    private final Long startAddress;
    private final int pageSize;
    private final int blockSize;

    private BitSet allocatedPages;

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

    public synchronized Long getPage() throws AllocatorException {
        Long output = null;

        for(int i = 0; i < blockSize; i += pageSize){
            int pageIndex = i / pageSize;

            if(!allocatedPages.get(pageIndex)) {
                allocatedPages.set(pageIndex);
                output = startAddress + i;
                break;
            }
        }

        if(output != null)
            return output;
        else
            throw new AllocatorException("No free pages in block");
    }

    /**
     * @param address
     * @throws AllocatorException
     * 
     * Method to free a page from the block.
     * 
     */

    public synchronized void freePage(Long address) throws AllocatorException, EmptyBlockException {
        Long relativeAddress = address - startAddress;

        if(relativeAddress < 0)
            throw new AllocatorException("Page not present in block");
        
        int pageIndex = (int) Math.floor(relativeAddress / pageSize);
        allocatedPages.set(pageIndex, false);
        
        if(allocatedPages.isEmpty())
            throw new EmptyBlockException("Block is empty");
    }
        
    /**
     * @param address
     * @return
     * 
     * Method to check if the block has free pages.
     * 
     */

    public synchronized boolean hasFreePages(){
        boolean output = false;
            
        for(int i = 0; i < blockSize / pageSize; i++) {
            if(!allocatedPages.get(i)) {
                output = true;
                break;
            }
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

    public synchronized boolean isAccessible(Long address) {
        Long relativeAddress = address - startAddress;

        if(relativeAddress >= 0) {
            int pageIndex = (int) Math.floor(relativeAddress / pageSize);
            return allocatedPages.get(pageIndex);
        }

        return false;
    }
}