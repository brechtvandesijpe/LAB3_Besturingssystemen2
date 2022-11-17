package Allocator;

public class Main {
 
    public static void main(String[] args) {
        Allocator allocator = AllocatorImplementation.getInstance();

        int amount = 8000;

        Long address = allocator.allocate(amount);
        System.out.println("Allocated " + amount + " bytes at " + address);

        System.out.println(allocator.isAccessible(address));
        
        allocator.free(address);
        System.out.println("Freed " + amount + " bytes at " + address);
    }

}