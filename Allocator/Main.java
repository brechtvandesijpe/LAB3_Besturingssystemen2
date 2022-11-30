package Allocator;

import Debugger.*;

public class Main {
    public static void main(String[] args) throws BlockException {
        BlockTester blocktester = new BlockTester(4);
        blocktester.test();
    }
}
