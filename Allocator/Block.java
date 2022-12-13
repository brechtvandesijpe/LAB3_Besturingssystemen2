package Allocator;

import java.util.BitSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

import Debugger.*;

public class Block {
    public static final int UNIT_BLOCK_SIZE = 4096;     // minimal block size

    private final Long startAddress;                    // start address of the block
    private final int pageSize;                         // size of the pages in the block in bytes
    private final int blockSize;                        // size of the block in bytes

    private AtomicIntegerArray allocatedPages;          // array of allocated pages
    private Logger logger;                              // logger to use

    /**
     * @param startAddress is the startaddress of the block
     * @param pageSize is the size of the pages in the block
     * 
     * Constructor for a block.
     */

    public Block(Long startAddress, int pageSize, int blockSize) throws BlockException {
        if(pageSize > blockSize)
            throw new BlockException("Page size can't be greater than block size");

        this.startAddress = startAddress;
        this.pageSize = pageSize;
        this.blockSize = blockSize;

        this.allocatedPages = new AtomicIntegerArray(blockSize / pageSize);
        logger = Logger.getInstance();
    }

    /**
     * @return the startaddress of the block
     * 
     * Get start address of the block.
     */

    public Long getStartAddress() {
        return startAddress;
    }

    /**
     * @return the size of the block
     * 
     * Get the size of the block.
     */

    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return the address of the page
     * @throws BlockException when there are no free pages in the block
     * 
     * Method to get a free page from the block.
     */

    public Long allocate() throws BlockException {
        for(int i = 0; i < blockSize; i += pageSize){
            int pageIndex = i / pageSize;
            
            // Check if the page is free
            if(allocatedPages.get(pageIndex) == 0){
                // Set the page as allocated
                allocatedPages.set(pageIndex,1);
                // Return the address of the page
                return startAddress + i;
            }
        }

        throw new BlockException("No free pages in block");
    }

    /**
     * @param address is the address that has to be freed
     * @throws BlockException when the block is empty after freeing the page
     * 
     * Method to free a page from the block.
     */

    public void free(Long address) throws BlockException {
        // Check if the address is within the block
        if(!isAccessible(address, 1))
            return;
        
        // Get the virtual address in the page
        address -= startAddress;

        // Get the page index of the relative address
        int pageIndex = (int) Math.floor(address / pageSize);

        // Free the page
        allocatedPages.set(pageIndex, 0);

        // If the block is empty, throw an exception
        for(int i = 0; i < (blockSize / pageSize); i++) {
            if(allocatedPages.get(i) == 1)
                return;
        }
        
        throw new BlockException("Block is empty");
    }

    /**
     * @return if the block has free pages
     * 
     * Method to check if the block has free pages.
     */

    public boolean hasFreePages(){
        for(int i = 0; i < (blockSize / pageSize); i++) {
            if(allocatedPages.get(i) == 0)
                return true;
        }

        return false;
    }

    /**
     * @param address is the address that has to be checked
     * @return if the address is allocated
     * 
     * Method to check if the block is accessible on a specific address.
     */

    public boolean isAccessible(Long address) {
        return isAccessible(address, 1);
    }

    /**
     * @param address is the address of the start of the range that has to be checked
     * @param range is the range that has to be checked
     * @return if the range is allocated
     * 
     * Method to check if the block is accessible on a specific address and a given range after that address.
     */

    public boolean isAccessible(Long address, int range) {
        address -= startAddress;

        if(range == 0)
            throw new IllegalArgumentException("Range is zero");

        if(address < 0 || address > blockSize || (address + range) < 0 || (address + range) > blockSize)
            return false;

        int pageIndexAddress = (int) Math.floor(address / pageSize);
        int pageIndexRange = (int) Math.floor((address + range - 1) / pageSize);

        if(pageIndexAddress != pageIndexRange)
            return false;

        return allocatedPages.get(pageIndexAddress) == 1;
    }
}