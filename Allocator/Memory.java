package Allocator;

import jdk.nashorn.internal.ir.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class Memory {
    // First fitting size, list of blocks
    private HashMap<Integer, List<Page>> memory;
    private static final int AMOUNT_OF_BLOCKS = 10;

    public Memory(){
        this.memory = new HashMap<>();
        for(int i = 3; i < 12; i++){
            memory.put((int) Math.pow(2, i), new ArrayList<>());
        }
    }

    public void addPage(Page p){
        int pageSize = p.getSize();

    }

    public int getFittingSize(int size){

    }


}


