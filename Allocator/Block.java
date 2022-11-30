package Allocator;

import java.util.BitSet;
import java.util.*;

import Debugger.*;

public class Block {
    public static final int UNIT_BLOCK_SIZE = 64;

    private final Long startAddress;        // start address of the block
    private final int pageSize;             // size of the pages in the block in bytes
    private final int blockSize;            // size of the block in bytes
    
    private BitSet allocatedPages;

    private Logger logger;

    /**
     * @param startAddress
     * @param pageSize
     * 
     * Constructor for a block.
     */

    public void print(String message) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < allocatedPages.size(); i++) {
            sb.append(allocatedPages.get(i) ? "1" : "0");
        }

        logger.log(message + " : " + sb.toString());
    }

    public Block(Long startAddress, int pageSize, int blockSize) {
        this.startAddress = startAddress;
        this.pageSize = pageSize;
        this.blockSize = blockSize;
        
        allocatedPages = new BitSet();
        logger = Logger.getInstance();
    }

    /**
     * @return
     * 
     * Get start address of the block.
     */

    public Long getStartAddress() {
        return startAddress;
    }

    /**
     * @return
     * 
     * Get the size of the block.
     */

    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return
     * @throws AllocatorException
     * 
     * Method to get a free page from the block.
     */

    public Long allocate() throws AllocatorException {
        for(int i = 0; i < blockSize; i += pageSize){
            int pageIndex = i / pageSize;
            
            // Check if the page is free
            if(!allocatedPages.get(pageIndex)){
                // Set the page as allocated
                allocatedPages.set(pageIndex);

                // Return the address of the page
                print("alloc");
                return startAddress + i;
            }
        }

        throw new AllocatorException("No free pages in block");

    }

    /**
     * @param address
     * @throws AllocatorException
     * 
     * Method to free a page from the block.
     */

    public void free(Long address) throws AllocatorException, EmptyBlockException {
        // Get the virtual address in the page
        Long relativeAddress = address - startAddress;

        // Check if the address is within the block
        if(relativeAddress < 0 || relativeAddress >= blockSize)
            throw new AllocatorException("Page not present in block");
        
        int pageIndex = (int) Math.floor(relativeAddress / pageSize);
        allocatedPages.set(pageIndex, false);
        
        if(allocatedPages.isEmpty())
            throw new EmptyBlockException("Block is empty");

        print("free");
    }

    /**
     * @param address
     * @return
     * 
     * Method to check if the block has free pages.
     */

    public boolean hasFreePages(){
        for(int i = 0; i < blockSize / pageSize; i++) {
            if(!allocatedPages.get(i))
                return true;
        }

        return false;
    }

    /**
     * @param address
     * @return
     * 
     * Method to check if the block is accessible on a specific address.
     */

    public boolean isAccessible(Long address) {
        Long relativeAddress = address - startAddress;

        if(relativeAddress < 0)
            return false;
        
        int pageIndex = (int) Math.floor(relativeAddress / pageSize);
        return allocatedPages.get(pageIndex);
    }
}