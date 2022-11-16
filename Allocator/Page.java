package Allocator;


import java.util.HashMap;

public class Page {
    final private static int PAGE_SIZE = 4096;
    private Long startAddress;
    private Long currentAddress;

    public Page(Long startAddress){
        this.startAddress = startAddress;
        addresses = new HashMap<>();
    }

    public Long add(Long address, int size){
        addresses.put(address, size);
        Long output = currentAddress;
        currentAddress += size;
        return output;
    }

    public void remove(Long address){
        addresses.remove(address);

    }

}
