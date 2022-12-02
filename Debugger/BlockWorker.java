package Debugger;

import Allocator.Block;
import Allocator.BlockException;

public class BlockWorker extends Thread {
    private Block block;

    private int amountOfIterations;

    private Logger logger;

    public BlockWorker(Block block, int amountOfIterations) {
        this.block = block;
        this.amountOfIterations = amountOfIterations;
        logger = Logger.getInstance();
    }

    public void testRange(Long startAddress, int range, boolean condition) throws TesterFailedException {
        if(block.isAccessible(startAddress, range) != condition) {
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range), 1);
            throw new TesterFailedException();
        }
    }

    public void run() throws RuntimeException {
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {}

        logger.log("running");

        try {
            for(int i = 0; i < amountOfIterations; i++) {
                try {
                    Long address = block.allocate();
                    testRange(address, 1, true);
                    block.free(address);
                    testRange(address, 1, false);
                } catch(BlockException e) {}
            }
        } catch(TesterFailedException e) {
            throw new RuntimeException();
        } 
    }
}