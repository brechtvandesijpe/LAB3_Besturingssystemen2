package Allocator;

import java.util.BitSet;
import java.util.*;

import Debugger.*;

public class Block {
    public static final int UNIT_BLOCK_SIZE = 16;

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

    public Block(Long startAddress, int pageSize, int blockSize) throws BlockException {
        if(pageSize > blockSize)
            throw new BlockException("Page size can't be greater than block size");

        this.startAddress = startAddress;
        this.pageSize = pageSize;
        this.blockSize = blockSize;

        
        allocatedPages = new BitSet();
        logger = Logger.getInstance();

        logger.log(this.pageSize + " " + this.blockSize);
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

    public Long allocate() throws BlockException {
        for(int i = 0; i < blockSize; i += pageSize){
            int pageIndex = i / pageSize;
            
            // Check if the page is free
            if(!allocatedPages.get(pageIndex)){
                // Set the page as allocated
                allocatedPages.set(pageIndex);

                // Return the address of the page
                return startAddress + i;
            }
        }

        throw new BlockException("No free pages in block");

    }

    /**
     * @param address
     * @throws AllocatorException
     * 
     * Method to free a page from the block.
     */

    public void free(Long address) throws BlockException {
        
        // Check if the address is within the block
        if(isAccessible(address, 1))
            return;
        
        // Get the virtual address in the page
        Long relativeAddress = address - startAddress;

        // Get the page index of the relative address
        int pageIndex = (int) Math.floor(relativeAddress / pageSize);

        // Free the page
        allocatedPages.set(pageIndex, false);
        
        // If the block is empty, throw an exception
        // if(allocatedPages.isEmpty())
        //     throw new BlockException("Block is empty");
    }

    /**
     * @param address
     * @return
     * 
     * Method to check if the block has free pages.
     */

    public boolean hasFreePages(){
        for(int i = 0; i < (blockSize / pageSize); i++) {
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
        return isAccessible(address, 1);
    }

    /**
     * @param address
     * @param range
     * @return
     * 
     * Method to check if the block is accessible on a specific address and a given range after that address.
     */

    public boolean isAccessible(Long address, int range) {
        Long relativeAddress = address - startAddress;

        if(relativeAddress < 0 || relativeAddress >= blockSize)
            return false;

        int pageIndex = (int) Math.floor(relativeAddress / pageSize);

        for(address = relativeAddress; relativeAddress < (address + range); address++) {
            int index = (int) Math.floor((address) / pageSize);
            if(index != pageIndex)
                return false;
        }
        
        return allocatedPages.get(pageIndex);
    }
    
    public void print(String message) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < (blockSize / pageSize); i++) {
            sb.append(allocatedPages.get(i) ? "1" : "0");
        }
        
        logger.log(sb.toString() + " : " + message);
    }
}