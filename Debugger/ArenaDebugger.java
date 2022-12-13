package Debugger;

import Allocator.Arena;
import Allocator.ArenaException;
import Allocator.Block;

public class ArenaDebugger {
    private int blockSize;                  // The size of the blocks
    private int pageSize;                   // The size of the pages
    private Arena arena;                    // The arena to test
    private Long address;                   // The address of the allocation
    private Logger logger;                  // The logger to use
    private String[] states;                // Buffer to store the states of the arena before a test fails
    
    public ArenaDebugger() {
        this.blockSize = 0;
        this.pageSize = 0;
        address = null;
        logger = Logger.getInstance();
    }

    /**
     * @param startAddress is the startaddress af the range to test
     * @param range is the range of addresses to test
     * @param condition is the condition that is expected
     * @throws DebuggerFailedException when the condition is not met
     * @return void
     * 
     * Test a condition for a range of addresses. If not met fail the debugger.
     */

    public void testRange(Long startAddress, int range, boolean condition) throws DebuggerFailedException {
        if(arena.isAccessible(startAddress, range) != condition) {
            printStates();
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range), 1);
            throw new DebuggerFailedException();
        }
    }

    /**
     * @return ovid
     * 
     * Print the trace of states during the test
     */

    private void printStates() {
        for(String s : states) {
            if(s != null) logger.log(s,1);
        }
    }

    /**
     * @throws DebuggerFailedException when a test fails
     * @throws DebuggerSuccessException when all tests are passed
     * @return void
     * 
     * Excecute the test for the debugging
     */

    public void test() throws DebuggerFailedException, DebuggerSuccessException {
        int[][] sizes = {{Block.UNIT_BLOCK_SIZE, 1},
                         {Block.UNIT_BLOCK_SIZE, 2},
                         {Block.UNIT_BLOCK_SIZE, 4},
                         {Block.UNIT_BLOCK_SIZE, 8},
                         {Block.UNIT_BLOCK_SIZE, 16},
                         {Block.UNIT_BLOCK_SIZE, 32},
                         {Block.UNIT_BLOCK_SIZE, 64},
                         {Block.UNIT_BLOCK_SIZE, 128},
                         {Block.UNIT_BLOCK_SIZE, 256},
                         {Block.UNIT_BLOCK_SIZE, 512},
                         {Block.UNIT_BLOCK_SIZE, 1024},
                         {Block.UNIT_BLOCK_SIZE, 2048}};

        for(int[] size : sizes) {
            states = new String[4];

            blockSize = size[0];
            pageSize = size[1];

            arena = new Arena(blockSize, pageSize);
            
            address = arena.allocate();
            states[0] = arena.toString();


            Long address2 = arena.allocate();
            states[1] = arena.toString();

            // Check a range of addresses address for address
            for(int offset = 0; offset < pageSize; offset++) {
                testRange(address + offset, 1, true);
                testRange(address2 + offset, 1, true);
            }

            // Check range Under->In block
            testRange(address - 1, 2, false);
            testRange(address2 - 1, 2, false);
            
            // Check range In->In block
            testRange(address, pageSize, true);
            testRange(address2, pageSize, true);

            // Check range In->Above block
            testRange(address, pageSize + 1, false);
            testRange(address2, pageSize + 1, false);

            try {
                arena.free(address2);
                states[2] = arena.toString();
            } catch(ArenaException e) {}
            
            // Check a range of addresses address for address
            for(int offset = 0; offset < pageSize; offset++) {
                testRange(address + offset, 1, true);
                testRange(address2 + offset, 1, false);
            }

            // Check range Under->In block
            testRange(address - 1, 2, false);
            testRange(address2 - 1, 2, false);
            
            // Check range In->In block
            testRange(address, pageSize, true);
            testRange(address2, pageSize, false);

            // Check range In->Above block
            testRange(address, pageSize + 1, false);
            testRange(address2, pageSize + 1, false);
            
            try {
                arena.free(address);
                states[3] = arena.toString();
            } catch(ArenaException e) {}

            // Check a range of addresses address for address
            for(int offset = 0; offset < pageSize; offset++) {
                testRange(address + offset, 1, false);
                testRange(address2 + offset, 1, false);
            }

            // Check range Under->In block
            testRange(address - 1, 2, false);
            testRange(address2 - 1, 2, false);
            
            // Check range In->In block
            testRange(address, pageSize, false);
            testRange(address2, pageSize, false);

            // Check range In->Above block
            testRange(address, pageSize + 1, false);
            testRange(address2, pageSize + 1, false);
            
            System.out.println("PASSED: Pagesize " + pageSize);
        }

        int[][] sizes2 = {{Block.UNIT_BLOCK_SIZE, Block.UNIT_BLOCK_SIZE},
                          {2 * Block.UNIT_BLOCK_SIZE, 2 * Block.UNIT_BLOCK_SIZE},
                          {3 * Block.UNIT_BLOCK_SIZE, 3 * Block.UNIT_BLOCK_SIZE},
                          {4 * Block.UNIT_BLOCK_SIZE, 4 * Block.UNIT_BLOCK_SIZE},
                          {5 * Block.UNIT_BLOCK_SIZE, 5 * Block.UNIT_BLOCK_SIZE}};

        for(int[] size : sizes2) {
            states = new String[2];

            blockSize = size[0];
            pageSize = size[1];

            arena = new Arena(blockSize);
            
            address = arena.allocate();
            states[0] = arena.toString();

            // Check a range of addresses address for address
            for(int offset = 0; offset < pageSize; offset++)
                testRange(address + offset, 1, true);

            // Check range Under->In block
            testRange(address - 1, 2, false);
            
            // Check range In->In block
            testRange(address, pageSize, true);

            // Check range In->Above block
            testRange(address, pageSize + 1, false);
            
            try {
                arena.free(address);
                states[1] = arena.toString();
            } catch(ArenaException e) {}

            // Check a range of addresses address for address
            for(int offset = 0; offset < pageSize; offset++) {
                testRange(address + offset, 1, false);
            }

            // Check range Under->In block
            testRange(address - 1, 2, false);
            
            // Check range In->In block
            testRange(address, pageSize, false);

            // Check range In->Above block
            testRange(address, pageSize + 1, false);

            System.out.println("PASSED: Pagesize " + pageSize);
        }
            
        throw new DebuggerSuccessException();
    }
}