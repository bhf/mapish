package com.bhf.mapish;

import com.bhf.mapish.serializers.IntegerSerializer;
import org.junit.jupiter.api.Test;
import java.lang.foreign.Arena;
import static org.junit.jupiter.api.Assertions.*;

public class OffHeapMapFullProbeTest {

    @Test
    public void testMapFullLinearProbeExhaustion() {
        try (Arena arena = Arena.ofConfined()) {
            
            // To prevent automatic resizing from interfering with our ability to completely 
            // fill up the capacity, we will override the map with a custom load factor bypass
            OffHeapMap<Integer, Integer> map = new OffHeapMap<Integer, Integer>(
                new IntegerSerializer(), new IntegerSerializer(), 2, arena, true) {
                    @Override
                    public Integer put(Integer key, Integer value) {
                        return super.insert(OffHeapMapFullProbeTest.getSegment(this), 2, key, value, true); // skip resize check by passing isResize=true
                    }
            };

            map.put(1, 100);
            map.put(2, 200);

            // Now the map is 100% full, physically. Any further operations will traverse the entire ring natively
            // and bottom out at the `return null` / `return -1` loop exhausts
            
            assertNull(map.get(3));
            assertNull(map.remove(3));
            assertFalse(map.containsKey(3));
        }
    }
    
    // Quick dirty reflection to get the private segment for our resize bypass test
    public static java.lang.foreign.MemorySegment getSegment(OffHeapMap<?, ?> map) {
        try {
            java.lang.reflect.Field f = OffHeapMap.class.getDeclaredField("memorySegment");
            f.setAccessible(true);
            return (java.lang.foreign.MemorySegment) f.get(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
