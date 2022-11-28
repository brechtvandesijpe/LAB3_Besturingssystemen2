package Allocator;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.LinkedList;

public class STAllocator implements Allocator {
    private NavigableMap<Integer, Arena> alloccedBlocks = new TreeMap<>();

    private LinkedList<Arena> getArenas() {
        LinkedList<Arena> output = new LinkedList<>();

        synchronized(alloccedBlocks) {
            for(Arena a : alloccedBlocks.values())
                if(a != alloccedBlocks) output.add(a);
        }

        return output;
    }

    private Arena getArena(Long address) {
        for(Arena arena : getArenas()) {
            boolean b = arena.isAccessible(address);
            if(b) return arena;
        }

        return null;
    }

    @Override
    public Long allocate(int size) {
        if(size <= 0)
            throw new AllocatorException("Size can't be negative or zero");
        
        Arena arena;
        
        synchronized(alloccedBlocks) {
            arena = alloccedBlocks.get(size);
        }
        
        if(arena == null) {
            if(size > 4096)
                arena = new Arena(Block.UNIT_BLOCK_SIZE);
            else
                arena = new Arena(Block.UNIT_BLOCK_SIZE, size);

            synchronized(alloccedBlocks) {
                alloccedBlocks.put(size, arena);
            }
        }

        return arena.getPage();
    }

    @Override
    public void free(Long address) throws AllocatorException {
        Arena arena = getArena(address);

        if(arena == null)
            throw new AllocatorException("Address is not allocated");

        arena.freePage(address);
    }

    @Override
    public Long reAllocate(Long oldAddress, int newSize) throws AllocatorException {
        if(newSize <= 0)
            throw new AllocatorException("Size can't be negative or zero");

        free(oldAddress);
        return allocate(newSize);
    }

    @Override
    public boolean isAccessible(Long address) {
        return getArena(address) != null;
    }

    @Override
    public boolean isAccessible(Long address, int size) {
        Arena arena;
        
        synchronized(alloccedBlocks) {
            arena = alloccedBlocks.get(size);
        }

        if(arena == null) return false;
        else return arena.isAccessible(address);
    }
}
