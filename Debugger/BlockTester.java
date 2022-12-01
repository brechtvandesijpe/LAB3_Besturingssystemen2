package Debugger;

import Allocator.Block;
import Allocator.BlockException;

public class BlockTester {
    private Block block;

    private int pageSize;

    private int amountOfPages;

    private Long address;

    private Logger logger;
    
    public BlockTester(int pageSize) throws BlockException {
        this.pageSize = pageSize;
        block = new Block(0L, pageSize);
        amountOfPages = Block.UNIT_BLOCK_SIZE / pageSize;
        address = null;
        logger = Logger.getInstance();
    }

    public void testRange(Long startAddress, int range, boolean condition) {
        if(block.isAccessible(startAddress, range) != condition) {
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range));
        }
    }

    public void test() throws BlockException {
        block.print("Before alloc");
        address = block.allocate();
        block.print("After alloc");

        // Enkele adressen checken
        for(Long i = address; i < (Long) (address + pageSize); i++) {
            testRange(i, 0, true);
        }

        block.print("Before alloc");
        Long address2 = block.allocate();
        block.print("After alloc");

        // Range Onder->In checken
        testRange(address - (amountOfPages / 2), amountOfPages, false);
        
        // Range In->In checken
        testRange(address, amountOfPages, true);
        
        // Range In->Boven checken7
        testRange(address + (amountOfPages / 2), amountOfPages, false);
        
        block.print("Before frees");
        block.free(address2);
        block.print("After free");
        block.free(address);
        block.print("After frees");

        // Enkele adressen checken
        for(Long i = address; i < (Long) (address + pageSize); i++) {
            testRange(i, 4, false);
        }

        // Range Onder->In checken
        testRange(address - (amountOfPages / 2), amountOfPages, false);

        // Range In->In checken
        testRange(address, amountOfPages, false);

        // Range In->Boven checken7
        testRange(address + (amountOfPages / 2), amountOfPages, false);
    }
}