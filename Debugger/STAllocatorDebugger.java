package Debugger;

import Allocator.STAllocator;
import java.util.Random;

public class STAllocatorDebugger {
    private STAllocator allocator;          // The allocator to test
    private Long address;                   // The address of the allocation
    private Logger logger;                  // The logger to use
    private int size;                       // The size of the allocation
    private String[] states;                // Buffer to store the states of the allocator before a test fails
    
    public STAllocatorDebugger() {
        allocator = new STAllocator();
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
        if(allocator.isAccessible(startAddress, range) != condition) {
            printStates();
            logger.log("Expected " + condition + " for address " + startAddress
                            + (range <= 1 ? "" : " and range " + range + " with size " + size), 1);
            throw new DebuggerFailedException();
        }
    }

    /**
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

    public void test() throws DebuggerSuccessException, DebuggerFailedException {
        int[] sizes = {1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192};

        for(int i = 0; i < sizes.length; i++) {
            size = sizes[i];
            int originalSize = size;
            states = new String[4];

            /*
             * ===========================================================================
             * ==============================ALLOCATION TEST==============================
             * ===========================================================================
             * 
             * Test the allocation of an address of size size
             */

            address = allocator.allocate(size);
            states[0] = allocator.toString();

            // Enkele adressen checken
            for(int offset = 0; offset < size; offset++)
                testRange(address + offset, 1, true);

            int range = (int) Math.ceil(size / 2);
            range = range == 0 ? 1 : range;
            
            // Range Onder->In checken
            testRange(address - 1, 2, false);
                        
            // Range In->In checken
            testRange(address, size, true);

            // Range In->Boven checken
            testRange(address, size + 1, false);

            System.out.println("PASSED: Pagesize " + originalSize + " MALLOC");

            /*
             * ===========================================================================
             * ============================REALLOCATION TEST 1============================
             * ===========================================================================
             * 
             * Test the reAllocation of an address when size doubles.
             */
            
            size *= 2;
            address = allocator.reAllocate(address, size);
            states[1] = allocator.toString();

            // Check a range of addresses address for address
            for(int offset = 0; offset < size; offset++) {
                testRange(address + offset, 1, true);
            }     

            // Check range Under->In block
            testRange(address - 1, 2, false);

            // Check range In->In block
            testRange(address, size, true);

            // Check range In->Above block
            testRange(address, size + 1, false);

            System.out.println("PASSED: Pagesize " + originalSize + " REALLOC+");

            /*
             * ===========================================================================
             * ============================REALLOCATION TEST 2============================
             * ===========================================================================
             * 
             * Test the reAllocation of an address when the size divides by two.
             */

            if(size != 2) {
                size /= 4;
                address = allocator.reAllocate(address, size);
                states[2] = allocator.toString();
                
                // Check a range of addresses address for address
                for(int offset = 0; offset < size; offset++) {
                    testRange(address + offset, 1, true);
                }

                // Check range Under->In block
                testRange(address - 1, 2, false);

                // Check range In->In block
                testRange(address, size, true);

                System.out.println("PASSED: Pagesize " + originalSize + " REALLOC-");

            }

            /*
             * ===========================================================================
             * =================================FREE TEST=================================
             * ===========================================================================
             * 
             * Test the freeing of an address.
             */

            allocator.free(address);
            states[3] = allocator.toString();

            // Check a range of addresses address for address
            for(int offset = 0; offset < size; offset++)
                testRange(address + offset, 1, false);

            // Check range Under->In block
            testRange(address - 1, 2, false);

            // Check range In->In block
            testRange(address, size, false);

            // Check range In->Above block
            testRange(address, size + 1, false);

            System.out.println("PASSED: Pagesize " + originalSize + " FREE");
            
        }

        /*
         * ===========================================================================
         * ================================STRESS TEST================================
         * ===========================================================================
         * 
         * Test the allocation, reallocation and freeing of a lot of random sizes.
         */

        Random random = new Random();

        int amountOfTests = 1000;

        Long[] addresses = new Long[amountOfTests];
        int[] sizesList = new int[amountOfTests];

        for(int i = 0; i < amountOfTests; i++) {
            size = random.nextInt(1, 20000);
            states = new String[4];

            states[0] = allocator.toString();
            address = allocator.allocate(size);
            states[0] = allocator.toString();

            // Check if the address is allocated
            testRange(address, size, true);

            addresses[i] = address;
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM MALLOC");

        for(int i = 0; i < amountOfTests; i++) {
            Long addr = addresses[i];
            size = random.nextInt(1, 20000);
            states = new String[4];

            states[0] = allocator.toString();
            address = allocator.reAllocate(addr, size);
            states[1] = allocator.toString();
            
            // Check if the address is still allocated
            testRange(address, size, true);
            
            addresses[i] = address;
            sizesList[i] = size;
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM REALLOC");

        for(int i = 0; i < amountOfTests; i++) {
            states = new String[4];

            Long addr = addresses[i];
            
            states[0] = allocator.toString();
            allocator.free(addr);
            states[1] = allocator.toString();

            size = sizesList[i];
            // Check if the address is freed
            testRange(addr, size, false);
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM FREE");
        
        throw new DebuggerSuccessException();
    }
}