package Debugger;

import Allocator.Arena;
import Allocator.AllocatorException;

public class ArenaTester {
    private Arena arena;

    private int blockSize;

    private int pageSize;

    private int amountOfPages;

    private Long address;

    private Logger logger;

    private boolean debug;
    
    public ArenaTester(int blockSize, int pageSize, boolean debug) {
        this.blockSize = blockSize;
        this.pageSize = pageSize;

        arena = new Arena(blockSize, pageSize);
        
        amountOfPages = blockSize / pageSize;
        address = null;

        logger = Logger.getInstance();

        this.debug = debug;
    }

    public void testRange(Long startAddress, int range, boolean condition) throws TesterException {
        if(arena.isAccessible(startAddress, range) != condition) {
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range));
            throw new TesterException("TEST FAILED");
        }
    }

    public void test() throws TesterException {
        if(debug) arena.print("Before alloc");
        address = arena.allocate();
        if(debug) arena.print("After alloc");

        // Enkele adressen checken
        for(Long i = address; i < (Long) (address + pageSize); i++) {
            testRange(i, 0, true);
        }

        if(debug) arena.print("Before alloc");
        Long address2 = arena.allocate();
        if(debug) arena.print("After alloc");

        // Range Onder->In checken
        testRange(address - (pageSize / 2), pageSize, false);
        
        // Range In->In checken
        testRange(address, pageSize, true);
        
        // Range In->Boven checken7
        testRange(address + (pageSize / 2), pageSize, false);
        
        if(debug) arena.print("Before frees");
        arena.free(address2);
        if(debug) arena.print("After free");
        arena.free(address);
        if(debug) arena.print("After frees");
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