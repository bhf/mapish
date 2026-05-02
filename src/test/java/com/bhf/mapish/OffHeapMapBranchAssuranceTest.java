package com.bhf.mapish;

import com.bhf.mapish.serializers.StringSerializer;
import org.junit.jupiter.api.Test;
import java.lang.foreign.Arena;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class OffHeapMapBranchAssuranceTest {

    @Test
    public void testMapEntryAndSetBranches() {
        try (Arena arena = Arena.ofConfined();
             OffHeapMap<String, String> confinedMap = new OffHeapMap<>(
                     new StringSerializer(64), new StringSerializer(64), 16, arena)) {

            confinedMap.put("A", "Alpha");
            confinedMap.put("B", "Beta");
            
            // Test EntrySet branches
            assertFalse(confinedMap.entrySet().contains("NotAnEntry"));
            assertFalse(confinedMap.entrySet().remove("NotAnEntry"));
            
            Map.Entry<String, String> dummyEntry = new Map.Entry<String, String>() {
                public String getKey() { return "C"; }
                public String getValue() { return "Gamma"; }
                public String setValue(String value) { return null; }
            };
            assertFalse(confinedMap.entrySet().contains(dummyEntry)); // Key doesn't exist
            
            Map.Entry<String, String> mismatchEntry = new Map.Entry<String, String>() {
                public String getKey() { return "A"; }
                public String getValue() { return "Gamma"; }
                public String setValue(String value) { return null; }
            };
            assertFalse(confinedMap.entrySet().contains(mismatchEntry)); // Value mismatch

            // Test Entry branches
            Iterator<Map.Entry<String, String>> it = confinedMap.entrySet().iterator();
            Map.Entry<String, String> entry = it.next();
            
            assertEquals("A=Alpha", entry.toString());
            assertTrue(entry.hashCode() != 0);
            
            // Equals branches
            assertFalse(entry.equals("String"));
            assertTrue(entry.equals(entry));
            
            Map.Entry<String, String> sameEntry = new Map.Entry<String, String>() {
                public String getKey() { return "A"; }
                public String getValue() { return "Alpha"; }
                public String setValue(String v) { return null; }
            };
            assertTrue(entry.equals(sameEntry));
            
            // Test exception branches
            assertThrows(NullPointerException.class, () -> entry.setValue(null));
            
            // Exhaust iterator
            it.next();
            final Iterator<Map.Entry<String, String>> finalIt = it;
            assertThrows(NoSuchElementException.class, () -> finalIt.next());
            
            // Iterator remove branch
            it = confinedMap.entrySet().iterator();
            it.next();
            it.remove();
            assertThrows(IllegalStateException.class, it::remove);
        }
    }

    @Test
    public void testMapCapacityExhaustion() {
        try (Arena arena = Arena.ofConfined();
             // Very small map, no resizing (threshold is disabled by forcing it)
             OffHeapMap<Integer, Integer> smallMap = new OffHeapMap<>(
                     new com.bhf.mapish.serializers.IntegerSerializer(),
                     new com.bhf.mapish.serializers.IntegerSerializer(),
                     2, arena)) {

            smallMap.put(1, 10);
            smallMap.put(2, 20); // Fills it perfectly right up to capacity
            
            // Will force the linear probing `for` loops inside the map entirely around the capacity circle
            assertNull(smallMap.get(3));
            assertFalse(smallMap.containsKey(3));
            assertNull(smallMap.remove(3));
        }
    }
}
