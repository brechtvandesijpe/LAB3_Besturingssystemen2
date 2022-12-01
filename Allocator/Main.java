package Allocator;

import Debugger.*;

public class Main {
    public static void main(String[] args) throws BlockException {
        Logger logger = Logger.getInstance();
        
        logger.log("====================================");
        logger.log("            BLOCK TESTER");
        logger.log("====================================");

        try {
            BlockTester blockTester = new BlockTester(64, 4, false);
            blockTester.test();
        } catch(TesterException e) {
            logger.log(e.getMessage());
        }

        logger.log("====================================");
        logger.log("            ARENA TESTER");
        logger.log("====================================");

        try {
            ArenaTester arenaTester = new ArenaTester(64, 4, false);
            arenaTester.test();
        } catch(TesterException e) {
            logger.log(e.getMessage());
        }
    }
}
