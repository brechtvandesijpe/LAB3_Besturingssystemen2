package Debugger;

import Allocator.BlockException;

public class Main {
    public static void main(String[] args) throws BlockException {
        System.out.println("============================================================================================================================");
        System.out.println("                                                       BLOCK DEBUGGER");
        
        try {
            BlockDebugger blockTester = new BlockDebugger();
            blockTester.test();
        } catch(DebuggerFailedException e) {
            System.out.println("                                                         TEST FAILED");
            System.out.println("============================================================================================================================");
            return;
        } catch(DebuggerSuccessException e) {
            System.out.println("                                                   ALL BLOCK TESTS PASSED");
            System.out.println("============================================================================================================================");
        }
        
        System.out.println("                                                       ARENA DEBUGGER");

        try {
            ArenaDebugger arenaTester = new ArenaDebugger();
            arenaTester.test();
        } catch(DebuggerFailedException e) {
            System.out.println("                                                         TEST FAILED");
            System.out.println("============================================================================================================================");
            return;
        } catch(DebuggerSuccessException e) {
            System.out.println("                                                   ALL ARENA TESTS PASSED");
            System.out.println("============================================================================================================================");
        }

            System.out.println("                                                      STALLOCATOR DEBUGGER");

        try {
            STAllocatorDebugger allocatorTester = new STAllocatorDebugger();
            allocatorTester.test();
        } catch(DebuggerFailedException e) {
            System.out.println("                                                         TEST FAILED");
            System.out.println("============================================================================================================================");
            return;
        } catch(DebuggerSuccessException e) {
            System.out.println("                                                 ALL STALLOCATOR TESTS PASSED");
            System.out.println("============================================================================================================================");
        }

        System.out.println("                                                     ALLOCATOR DEBUGGER");

        try {
            AllocatorDebugger allocatorTester = new AllocatorDebugger();
            allocatorTester.test();
        } catch(DebuggerFailedException e7) {
            System.out.println("                                                         TEST FAILED");
            System.out.println("============================================================================================================================");
        } catch(DebuggerSuccessException e) {
            System.out.println("                                                 ALL ALLOCATOR TESTS PASSED");
            System.out.println("============================================================================================================================");
        }
    }
}
