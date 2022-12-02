package Debugger;

import Allocator.Arena;
import Allocator.ArenaException;

public class ArenaWorker extends Thread {
    private Arena arena;

    private int amountOfIterations;

    private Logger logger;

    public ArenaWorker(Arena arena, int amountOfIterations) {
        this.arena = arena;
        this.amountOfIterations = amountOfIterations;
        logger = Logger.getInstance();
    }

    public void testRange(Long startAddress, int range, boolean condition) throws RuntimeException {
        if(arena.isAccessible(startAddress, range) != condition) {
            logger.log("Expected " + condition + " for address " + startAddress + (range <= 1 ? "" : " and range " + range), 1);
            throw new RuntimeException();
        }
    }

    public void run() throws RuntimeException {
        for(int i = 0; i < amountOfIterations; i++) {
            try {
                Long address = arena.allocate();
                testRange(address, 1, true);
                arena.free(address);
                testRange(address, 1, false);
            } catch(ArenaException e) {}
        }
    }
}