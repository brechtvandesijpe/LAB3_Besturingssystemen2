package Debugger;

import Allocator.Block;
import Allocator.BlockException;

public class BlockTester {
    private Block block;

    private int blockSize;

    private int pageSize;

    private int amountOfPages;

    private Long address;

    private Logger logger;

    private boolean debug;
    
    public BlockTester(int blockSize, int pageSize, boolean debug) throws BlockException {
        this.blockSize = blockSize;
        this.pageSize = pageSize;

        block = new Block(0L, pageSize, blockSize);
        
        amountOfPages = blockSize / pageSize;
        address = null;
        
        logger = Logger.getInstance();

        this.debug = debug;
    }

    public void testRange(Long startAddress, int range, boolean condition) throws TesterException {
        if(block.isAccessible(startAddress, range) != condition) {
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range));
            throw new TesterException("TEST FAILED");
        }
    }

    public void test() throws BlockException, TesterException {
        if(debug) block.print("Before alloc");
        address = block.allocate();
        if(debug) block.print("After alloc");

        // Enkele adressen checken
        for(Long i = address; i < (Long) (address + pageSize); i++) {
            testRange(i, 0, true);
        }

        if(debug) block.print("Before alloc");
        Long address2 = block.allocate();
        if(debug) block.print("After alloc");

        // Range Onder->In checken
        testRange(address - (pageSize / 2), pageSize, false);
        
        // Range In->In checken
        testRange(address, pageSize, true);
        
        // Range In->Boven checken7
        testRange(address + (pageSize / 2), pageSize, false);
        
        if(debug) block.print("Before frees");
        block.free(address2);
        if(debug) block.print("After free");
        block.free(address);
        if(debug) block.print("After frees");

        // Enkele adressen checken
        for(Long i = address; i < (Long) (address + pageSize); i++) {
            testRange(i, 4, false);
        }

        // Range Onder->In checken
        testRange(address - (pageSize / 2), pageSize, false);

        // Range In->In checken
        testRange(address, pageSize, false);

        // Range In->Boven checken7
        testRange(address + (pageSize / 2), pageSize, false);

        throw new TesterException("            TESTS PASSED");
    }

}