package com.bhf.mapish;

import com.bhf.mapish.serializers.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OffHeapMapLifecycleTest {

    @Test
    public void testMapClosureFreesMemory() {
        OffHeapMap<String, String> map;
        try (OffHeapMap<String, String> confinedMap = OffHeapMap.createConfined(
                new StringSerializer(64), new StringSerializer(64), 100)) {
            
            confinedMap.put("test_key", "test_value");
            assertEquals("test_value", confinedMap.get("test_key"));
            
            // Assign out of scope slightly to test post-destruction reference exceptions
            map = confinedMap;
        }

        // Using map after close should throw IllegalStateException from the underlying MemorySegment because the scope is no longer alive
        assertThrows(IllegalStateException.class, () -> {
            map.get("test_key");
        }, "Should throw an exception since the Arena backing the MemorySegment was closed.");
        
        assertThrows(IllegalStateException.class, () -> {
            map.put("another_key", "fail_value");
        }, "Should throw an exception since the MemorySegment is inaccessible.");
    }

    @Test
    public void testMapResizeAndCapacityExpansion() {
        // Default capacity is 16, load factor is 0.5 (threshold = 8)
        // Inserting 50 items will force multiple off-heap segment reallocations
        OffHeapMap<Integer, Integer> map = new OffHeapMap<>(new IntegerSerializer(), new IntegerSerializer());
        
        for (int i = 0; i < 50; i++) {
            map.put(i, i * 10);
        }
        
        assertEquals(50, map.size(), "Size should properly reflect all insertions across resizes");
        
        for (int i = 0; i < 50; i++) {
            assertEquals(i * 10, map.get(i), "Should retrieve correctly after resizing memory segments");
        }
        
        // Ensure removal still works after resizing
        map.remove(25);
        assertNull(map.get(25), "Removed element should be null");
        assertEquals(49, map.size());
    }
}
