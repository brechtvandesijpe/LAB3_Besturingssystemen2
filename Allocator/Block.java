package Allocator;

import java.util.BitSet;

public class Block {
    public static final int BLOCK_SIZE = 4096;

    private final Long startAddress;
    private final int pageSize;
    private final Long rootAddress;
    
    private BitSet allocatedPages;

    /**
     * 
     * @param startAddress
     * @param pageSize
     * 
     * COnstructor for a block.
     * 
     */

    public Block(Long startAddress, int pageSize){
        this.startAddress = startAddress;
        this.pageSize = pageSize;
        allocatedPages = new BitSet();
        this.rootAddress = startAddress;
    }

    /**
     * 
     * @param startAddress
     * @param pageSize
     * @param rootAddress
     * 
     * Constructor for a block with a different root address than  startAddress.
     */

    public Block(Long startAddress, int pageSize, Long rootAddress){
        this.startAddress = startAddress;
        this.pageSize = pageSize;
        allocatedPages = new BitSet();
        this.rootAddress = rootAddress;
    }

    /**
     * @return
     * @throws AllocatorException
     * 
     * Method to get a free page from the block.
     *
     */

    public Long getPage() throws AllocatorException {
        for(int i = 0; i < BLOCK_SIZE; i += pageSize){
            int pageIndex = i / pageSize;
            if(!allocatedPages.get(pageIndex)){
                allocatedPages.set(pageIndex);
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
     * 
     */

    public void freePage(Long address) throws AllocatorException {
        Long relativeAddress = address - startAddress;

        if(relativeAddress < 0)
            throw new AllocatorException("Page not present in block");
        
        int pageIndex = (int) Math.floor(relativeAddress / pageSize);
        allocatedPages.set(pageIndex, false);
    }

    /**
     * @param address
     * @return
     * 
     * Method to check if the block has free pages.
     * 
     */

    public boolean hasFreePages(){
        for(int i = 0; i < BLOCK_SIZE / pageSize; i++) {
            if(allocatedPages.get(i))
                return true;
        }

        return false;
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

        if(relativeAddress < 0)
            return false;
        
        int pageIndex = (int) Math.floor(relativeAddress / pageSize);
        return allocatedPages.get(pageIndex);
    }
}