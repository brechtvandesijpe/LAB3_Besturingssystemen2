package Debugger;

import Allocator.BlockException;

public class Main {
    public static void main(String[] args) throws BlockException {
        System.out.println("============================================================================================================================");
        System.out.println("                                                        BLOCK TESTER");
        
        try {
            BlockTester blockTester = new BlockTester(false);
            blockTester.test();
        } catch(TesterFailedException e) {
            System.out.println("                                                         TEST FAILED");
            System.out.println("============================================================================================================================");
            return;
        } catch(TesterSuccessException e) {
            System.out.println("                                                   ALL BLOCK TESTS PASSED");
            System.out.println("============================================================================================================================");
        }
        
        System.out.println("                                                        ARENA TESTER");

        try {
            ArenaTester arenaTester = new ArenaTester(false);
            arenaTester.test();
        } catch(TesterFailedException e) {
            System.out.println("                                                         TEST FAILED");
            System.out.println("============================================================================================================================");
            return;
        } catch(TesterSuccessException e) {
            System.out.println("                                                   ALL ARENA TESTS PASSED");
            System.out.println("============================================================================================================================");
        }

        System.out.println("                                                     STALLOCATOR TESTER");

        try {
            STAllocatorTester allocatorTester = new STAllocatorTester(false);
            allocatorTester.test();
        } catch(TesterFailedException e) {
            System.out.println("                                                         TEST FAILED");
            System.out.println("============================================================================================================================");
            return;
        } catch(TesterSuccessException e) {
            System.out.println("                                                ALL STALLOCATOR TESTS PASSED");
            System.out.println("============================================================================================================================");
        }

        System.out.println("                                                      ALLOCATOR TESTER");

        try {
            AllocatorTester allocatorTester = new AllocatorTester(false);
            allocatorTester.test();
        } catch(TesterFailedException e7) {
            System.out.println(e7.getMessage());
            System.out.println("============================================================================================================================");
            if(e7.getMessage().contains("FAIL")) return;
        } catch(TesterSuccessException e) {
            System.out.println("                                                 ALL ALLOCATOR TESTS PASSED");
            System.out.println("============================================================================================================================");
        }
    }
}
