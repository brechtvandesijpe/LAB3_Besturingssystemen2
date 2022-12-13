package Debugger;

import Allocator.Allocator;
import java.util.Random;

public class AllocatorDebugger {
    private Allocator allocator;            // The allocator to test
    private Long address;                   // The address of the allocation
    private Logger logger;                  // The logger to use
    private int size;                       // The size of the allocation
    private String[] states;                // Buffer to store the states of the allocator before a test fails
    
    public AllocatorDebugger() {
        allocator = Allocator.instance;
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

    public void test() throws DebuggerFailedException, DebuggerSuccessException {
        int[] sizes = {1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192};

        for(int i = 0; i < sizes.length; i++) {
            size = sizes[i];
            int originalSize = size;
            states = new String[4];
            Thread.currentThread().setName("1");

            /*
             * ===========================================================================
             * =======================ALLOCATION TEST (SAME THREAD)=======================
             * ===========================================================================
             * 
             * This test will allocate a random amount of memory, reallocate it to a
             * smaller then before and bigger then before size and free it again. All in
             * the same thread.
             */

            address = allocator.allocate(size);
            states[0] = allocator.toString();

            // Check a range of addresses address for address
            for(int offset = 0; offset < size; offset++)
                testRange(address + offset, 1, true);

            int range = (int) Math.ceil(size / 2);
            range = range == 0 ? 1 : range;
            
            // Check range Under->In block
            testRange(address - 1, 2, false);

            // Check range In->In block
            testRange(address, size, true);

            // Check range In->Above block
            testRange(address, size + 1, false);

            System.out.println("PASSED: Pagesize " + originalSize + " MALLOC (SAME THREAD)");

            /*
             * ===========================================================================
             * =====================REALLOCATION TEST 1 (SAME THREAD)=====================
             * ===========================================================================
             * 
             * This test will allocate a random amount of memory, reallocate it to a
             * smaller then before and bigger then before size and free it again. All in
             * the same thread.
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

            System.out.println("PASSED: Pagesize " + originalSize + " REALLOC+ (SAME THREAD)");

            /*
             * ===========================================================================
             * =====================REALLOCATION TEST 2 (SAME THREAD)=====================
             * ===========================================================================
             * 
             * This test will allocate a random amount of memory, reallocate it to a
             * smaller then before and bigger then before size and free it again. All in
             * the same thread.
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
            }

            System.out.println("PASSED: Pagesize " + originalSize + " REALLOC- (SAME THREAD)");

            /*
             * ===========================================================================
             * ==========================FREE TEST (SAME THREAD)==========================
             * ===========================================================================
             * 
             * This test will allocate a random amount of memory, reallocate it to a
             * smaller then before and bigger then before size and free it again. All in
             * the same thread.
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

            System.out.println("PASSED: Pagesize " + originalSize + " FREE (SAME THREAD)");
        }

        /*
         * ===========================================================================
         * =====================RANDOM STRESS TESTS (SAME THREAD)=====================
         * ===========================================================================
         * 
         * This test will allocate a random amount of memory, reallocate it to a
         * smaller then before and bigger then before size and free it again. All in
         * the same thread.
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

            // Check if address is allocated
            testRange(address, size, true);

            addresses[i] = address;
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM MALLOC (SAME THREAD)");

        for(int i = 0; i < amountOfTests; i++) {
            Long addr = addresses[i];
            size = random.nextInt(1, 20000);
            states = new String[4];

            states[0] = allocator.toString();
            address = allocator.reAllocate(addr, size);
            states[1] = allocator.toString();
            
            // Make sure aaddress is still allocated
            testRange(address, size, true);
            
            addresses[i] = address;
            sizesList[i] = size;
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM REALLOC (SAME THREAD)");

        for(int i = 0; i < amountOfTests; i++) {
            states = new String[4];

            Long addr = addresses[i];
            
            states[0] = allocator.toString();
            allocator.free(addr);
            states[1] = allocator.toString();

            size = sizesList[i];

            // Make sure qddress was freed
            testRange(addr, size, false);
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM FREE (SAME THREAD)");

        for(int i = 0; i < sizes.length; i++) {
            size = sizes[i];
            int originalSize = size;
            states = new String[4];

            /*
             * ===========================================================================
             * ====================ALLOCATION TEST (DIFFERENT THREADS)====================
             * ===========================================================================
             * 
             * This test will stress test the allocator by allocating in one tthread, re-
             * allocating in another, reallocating in a third thread and freeing in a
             * fourth. The threads are simulated by changing the thread name.
             */

            Thread.currentThread().setName("1");

            address = allocator.allocate(size);
            states[0] = allocator.toString();

            // Check a range of addresses address for address
            for(int offset = 0; offset < size; offset++)
                testRange(address + offset, 1, true);

            int range = (int) Math.ceil(size / 2);
            range = range == 0 ? 1 : range;
            
            // Check range Under->In block
            testRange(address - 1, 2, false);
            
            // Check range In->In block
            testRange(address, size, true);

            // Check range In->Above block
            testRange(address, size + 1, false);

            System.out.println("PASSED: Pagesize " + originalSize + " MALLOC (SAME THREAD)");

            /*
             * ===========================================================================
             * ==================REALLOCATION TEST 1 (DIFFERENT THREADS)==================
             * ===========================================================================
             * 
             * This test will stress test the allocator by allocating in one thread, re-
             * allocating in another, reallocating in a third thread and freeing in a
             * fourth. The threads are simulated by changing the thread name.
             */

            Thread.currentThread().setName("2");
            
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

            System.out.println("PASSED: Pagesize " + originalSize + " REALLOC+ (SAME THREAD)");

            /*
             * ===========================================================================
             * ===================REALLOCATION TEST 2 (DIFFERENT THREADS)=================
             * ===========================================================================
             * 
             * This test will stress test the allocator by allocating in one tthread, re-
             * allocating in another, reallocating in a third thread and freeing in a
             * fourth. The threads are simulated by changing the thread name.
             */

            Thread.currentThread().setName("3");

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
            }

            System.out.println("PASSED: Pagesize " + originalSize + " REALLOC- (SAME THREAD)");

            /*
             * ===========================================================================
             * ==========================FREE TEST (DIFFERENT THREADS)====================
             * ===========================================================================
             * 
             * This test will stress test the allocator by allocating in one tthread, re-
             * allocating in another, reallocating in a third thread and freeing in a
             * fourth. The threads are simulated by changing the thread name.
             */

            Thread.currentThread().setName("4");

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

            System.out.println("PASSED: Pagesize " + originalSize + " FREE (SAME THREAD)");
        }

        /*
         * ===========================================================================
         * ===================RANDOM STRESS TESTS (DIFFERENT THREADS)=================
         * ===========================================================================
         * 
         * This test will stress test the allocator by allocating in one tthread, re-
         * allocating in another, reallocating in a third thread and freeing in a
         * fourth. The threads are simulated by changing the thread name.
         */

        Thread.currentThread().setName("1");

        addresses = new Long[amountOfTests];
        sizesList = new int[amountOfTests];

        
        for(int i = 0; i < amountOfTests; i++) {
            size = random.nextInt(1, 20000);
            states = new String[4];

            states[0] = allocator.toString();
            address = allocator.allocate(size);
            states[0] = allocator.toString();

            // Check if allocated
            testRange(address, size, true);

            addresses[i] = address;
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM MALLOC (DIFFERENT THREAD)");

        Thread.currentThread().setName("2");

        for(int i = 0; i < amountOfTests; i++) {
            Long addr = addresses[i];
            size = random.nextInt(1, 20000);
            states = new String[4];

            states[0] = allocator.toString();
            address = allocator.reAllocate(addr, size);
            states[1] = allocator.toString();
            
            // Check if still allocated
            testRange(address, size, true);
            
            addresses[i] = address;
            sizesList[i] = size;
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM REALLOC (DIFFERENT THREAD)");

        Thread.currentThread().setName("3");

        for(int i = 0; i < amountOfTests; i++) {
            states = new String[4];

            Long addr = addresses[i];
            
            states[0] = allocator.toString();
            allocator.free(addr);
            states[1] = allocator.toString();

            size = sizesList[i];
            // make sure address is freed
            testRange(addr, size, false);
        }

        System.out.println("PASSED: " + amountOfTests + " RANDOM FREE (DIFFERENT THREAD)");
        
        throw new DebuggerSuccessException();
    }
}