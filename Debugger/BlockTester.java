package Debugger;

import Allocator.Block;
import Allocator.BlockException;

public class BlockTester {
    private int blockSize;
    
    private int pageSize;
    
    private int amountOfPages;

    private Block block;
    
    private Long address;
    
    private Logger logger;
    
    private boolean debug;

    private String[] states;
    
    public BlockTester(boolean debug) throws BlockException {
        address = null;
        
        logger = Logger.getInstance();

        this.debug = debug;
    }

    public void testRange(Long startAddress, int range, boolean condition) throws TesterException {
        if(block.isAccessible(startAddress, range) != condition) {
            printStates();
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range), 1);
            throw new TesterException("                                                         TEST FAILED");
        }
    }

    private void printStates() {
        for(String s : states) {
            if(s != null) logger.log(s,1);
        }
    }

    public void test() throws BlockException, TesterException {
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

            // Enkele adressen checken
            for(int offset = 0; offset < pageSize; offset++) {
                testRange(address + offset, 1, true);
                testRange(address2 + offset, 1, true);
            }

            // Range Onder->In checken
            testRange(address - 1, 2, false);
            testRange(address2 - 1, 2, false);
            
            // Range In->In checken
            testRange(address, pageSize, true);
            testRange(address2, pageSize, true);

            // Range In->Boven checken
            testRange(address, pageSize + 1, false);
            testRange(address2, pageSize + 1, false);

            try {
                block.free(address2);
                states[2] = block.toString();
            } catch (BlockException e) {}

            
            // Enkele adressen checken
            for(int offset = 0; offset < pageSize; offset++) {
                testRange(address + offset, 1, true);
                testRange(address2 + offset, 1, false);
            }

            // Range Onder->In checken
            testRange(address - 1, 2, false);
            testRange(address2 - 1, 2, false);
            
            // Range In->In checken
            testRange(address, pageSize, true);
            testRange(address2, pageSize, false);

            // Range In->Boven checken
            testRange(address, pageSize + 1, false);
            testRange(address2, pageSize + 1, false);
            
            try {
                block.free(address);
                states[3] = block.toString();
            } catch (BlockException e) {}

            // Enkele adressen checken
            for(int offset = 0; offset < pageSize; offset++) {
                testRange(address + offset, 1, false);
                testRange(address2 + offset, 1, false);
            }

            // Range Onder->In checken
            testRange(address - 1, 2, false);
            testRange(address2 - 1, 2, false);
            
            // Range In->In checken
            testRange(address, pageSize, false);
            testRange(address2, pageSize, false);

            // Range In->Boven checken
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

            // Enkele adressen checken
            for(int offset = 0; offset < pageSize; offset++)
                testRange(address + offset, 1, true);

            // Range Onder->In checken
            testRange(address - 1, 2, false);
            
            // Range In->In checken
            testRange(address, pageSize, true);

            // Range In->Boven checken
            testRange(address, pageSize + 1, false);
            
            try {
                block.free(address);
                states[1] = block.toString();
            } catch (BlockException e) {}

            // Enkele adressen checken
            for(int offset = 0; offset < pageSize; offset++) {
                testRange(address + offset, 1, false);
            }

            // Range Onder->In checken
            testRange(address - 1, 2, false);
            
            // Range In->In checken
            testRange(address, pageSize, false);

            // Range In->Boven checken
            testRange(address, pageSize + 1, false);

            System.out.println("PASSED: Pagesize " + pageSize);
        }
        
        throw new TesterException("                                                   ALL BLOCK TESTS PASSED");
    }
}