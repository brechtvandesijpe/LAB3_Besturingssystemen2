package Debugger;

import Allocator.Block;
import Allocator.BlockException;
import java.util.Random;
import java.util.LinkedList;

public class BlockDebugger {
    private int blockSize;
    
    private int pageSize;
    
    private int amountOfPages;

    private Block block;
    
    private Long address;
    
    private Logger logger;
    
    private boolean debug;

    private String[] states;
    
    public BlockDebugger(boolean debug) throws BlockException {
        address = null;
        
        logger = Logger.getInstance();

        this.debug = debug;
    }

    /**
     * @param startAddress
     * @param range
     * @param condition
     * @throws TesterFailedException
     * @return void
     * 
     * Test a condition for a range of addresses. If not met fail the debugger.
     */

    public void testRange(Long startAddress, int range, boolean condition) throws TesterFailedException {
        if(block.isAccessible(startAddress, range) != condition) {
            printStates();
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range), 1);
            throw new TesterFailedException("                                                         TEST FAILED");
        }
    }

    /**
     * Print the trace of states during the test
     */

    private void printStates() {
        for(String s : states) {
            if(s != null) logger.log(s,2);
        }
    }

    /**
     * @throws BlockException
     * @throws TesterFailedException
     * @throws TesterSuccessException
     * @return void
     * 
     * Excecute the test for the debugging
     */

    public void test() throws BlockException, TesterFailedException, TesterSuccessException {

        /*
         * ===========================================================================
         * ============================SINGLETHREADED TEST============================
         * ===========================================================================
         */

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

            block = new Block(0L, pageSize, blockSize);
            
            amountOfPages = blockSize / pageSize;
            address = block.allocate();
            states[0] = block.toString();


            Long address2 = block.allocate();
            states[1] = block.toString();

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
                block.free(address2);
                states[2] = block.toString();
            } catch (BlockException e) {}

            
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
                block.free(address);
                states[3] = block.toString();
            } catch (BlockException e) {}

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

            block = new Block(0L, pageSize, blockSize);
            
            amountOfPages = blockSize / pageSize;
            address = block.allocate();
            states[0] = block.toString();

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
                block.free(address);
                states[1] = block.toString();
            } catch (BlockException e) {}

            // Check a range of addresses address for address
            for(int offset = 0; offset < pageSize; offset++)
                testRange(address + offset, 1, false);

            // Check range Under->In block
            testRange(address - 1, 2, false);
            
            // Check range In->In block
            testRange(address, pageSize, false);

            // Check range In->Above block
            testRange(address, pageSize + 1, false);

            System.out.println("PASSED: Pagesize " + pageSize);
        }

        pageSize = 8;
        block = new Block(0L, pageSize, Block.UNIT_BLOCK_SIZE);
        LinkedList<Long> addresses = new LinkedList<>();
        
        boolean fullFLag = false;
        boolean emptyFlag = false;

        try {
            for(int i = 0; i < 513; i++) {
                address = block.allocate();
                addresses.add(address);

                // Check a range of addresses address for address
                for(int offset = 0; offset < pageSize; offset++)
                    testRange(address + offset, 1, true);

                // Check range Under->In block
                testRange(address - 1, 2, false);
                
                // Check range In->In block
                testRange(address, pageSize, true);

                // Check range In->Above block
                testRange(address, pageSize + 1, false);
            }
        } catch(BlockException e) {
            // When the Block is full, it should throw an exception to guarantee the working of the allocator
            fullFLag = true;
        }

        try {
            for(Long a : addresses) {
                block.free(a);

                // Check a range of addresses address for address
                for(int offset = 0; offset < pageSize; offset++)
                    testRange(a + offset, 1, false);

                // Check range Under->In block
                testRange(a - 1, 2, false);
                
                // Check range In->In block
                testRange(a, pageSize, false);

                // Check range In->Above block
                testRange(a, pageSize + 1, false);
            }
        } catch(BlockException e) {
            // BLock MUST throw up an exception when empty to guarantee the working of the allocator
            emptyFlag = true;

            if(fullFLag)
                System.out.println("PASSED: Random allocation");
            else {
                logger.log("Not all addresses were allocated when they should have been");
                throw new TesterFailedException();
            }
        }

        if(!emptyFlag) {
            logger.log("Not all addresses were freed when they should have been");
            throw new TesterFailedException();
        }

        throw new TesterSuccessException();
    }
}