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
}
