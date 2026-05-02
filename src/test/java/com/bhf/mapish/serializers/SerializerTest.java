package com.bhf.mapish.serializers;

import org.junit.jupiter.api.Test;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import static org.junit.jupiter.api.Assertions.*;

public class SerializerTest {

    @Test
    public void testIntegerSerializer() {
        IntegerSerializer serializer = new IntegerSerializer();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize(42, segment, 0);
            assertEquals(42, serializer.deserialize(segment, 0));
            assertTrue(serializer.equals(42, segment, 0));
            assertFalse(serializer.equals(43, segment, 0));
            assertFalse(serializer.equals(null, segment, 0));
        }
    }

    @Test
    public void testShortSerializer() {
        ShortSerializer serializer = new ShortSerializer();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize((short) 42, segment, 0);
            assertEquals((short) 42, serializer.deserialize(segment, 0));
            assertTrue(serializer.equals((short) 42, segment, 0));
            assertFalse(serializer.equals((short) 43, segment, 0));
            assertFalse(serializer.equals(null, segment, 0));
        }
    }

    @Test
    public void testByteSerializer() {
        ByteSerializer serializer = new ByteSerializer();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize((byte) 42, segment, 0);
            assertEquals((byte) 42, serializer.deserialize(segment, 0));
            assertTrue(serializer.equals((byte) 42, segment, 0));
            assertFalse(serializer.equals((byte) 43, segment, 0));
            assertFalse(serializer.equals(null, segment, 0));
        }
    }

    @Test
    public void testFloatSerializer() {
        FloatSerializer serializer = new FloatSerializer();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize(42.5f, segment, 0);
            assertEquals(42.5f, serializer.deserialize(segment, 0));
            assertTrue(serializer.equals(42.5f, segment, 0));
            assertFalse(serializer.equals(43.5f, segment, 0));
            assertFalse(serializer.equals(null, segment, 0));
        }
    }

    @Test
    public void testDoubleSerializer() {
        DoubleSerializer serializer = new DoubleSerializer();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize(42.5, segment, 0);
            assertEquals(42.5, serializer.deserialize(segment, 0));
            assertTrue(serializer.equals(42.5, segment, 0));
            assertFalse(serializer.equals(43.5, segment, 0));
            assertFalse(serializer.equals(null, segment, 0));
        }
    }

    @Test
    public void testCharacterSerializer() {
        CharacterSerializer serializer = new CharacterSerializer();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize('A', segment, 0);
            assertEquals('A', serializer.deserialize(segment, 0));
            assertTrue(serializer.equals('A', segment, 0));
            assertFalse(serializer.equals('B', segment, 0));
            assertFalse(serializer.equals(null, segment, 0));
        }
    }

    @Test
    public void testBooleanSerializer() {
        BooleanSerializer serializer = new BooleanSerializer();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize(true, segment, 0);
            assertTrue(serializer.deserialize(segment, 0));
            assertTrue(serializer.equals(true, segment, 0));
            assertFalse(serializer.equals(false, segment, 0));
            assertFalse(serializer.equals(null, segment, 0));

            serializer.serialize(false, segment, 0);
            assertFalse(serializer.deserialize(segment, 0));
            assertTrue(serializer.equals(false, segment, 0));
            assertFalse(serializer.equals(true, segment, 0));
        }
    }

    @Test
    public void testLongSerializer() {
        LongSerializer serializer = new LongSerializer();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize(42L, segment, 0);
            assertEquals(42L, serializer.deserialize(segment, 0));
            assertTrue(serializer.equals(42L, segment, 0));
            assertFalse(serializer.equals(43L, segment, 0));
            assertFalse(serializer.equals(null, segment, 0));
        }
    }

    @Test
    public void testStringSerializer() {
        StringSerializer serializer = new StringSerializer(16);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(serializer.sizeBytes());
            serializer.serialize("Hello", segment, 0);
            assertEquals("Hello", serializer.deserialize(segment, 0));
            assertTrue(serializer.equals("Hello", segment, 0));
            assertFalse(serializer.equals("World", segment, 0));
            assertFalse(serializer.equals(null, segment, 0));
            
            // Length mismatch in equals
            assertFalse(serializer.equals("Hell", segment, 0));
            
            // Empty string
            serializer.serialize("", segment, 0);
            assertEquals("", serializer.deserialize(segment, 0));
            assertTrue(serializer.equals("", segment, 0));
            
            // Exception: Exceeds max bytes
            assertThrows(IllegalArgumentException.class, () -> serializer.serialize("This string is way too long for 16 bytes", segment, 0));
            
            // Exception: Corrupted length in deserialize
            segment.set(java.lang.foreign.ValueLayout.JAVA_INT, 0, -1);
            assertThrows(IllegalStateException.class, () -> serializer.deserialize(segment, 0));
            
            segment.set(java.lang.foreign.ValueLayout.JAVA_INT, 0, 17);
            assertThrows(IllegalStateException.class, () -> serializer.deserialize(segment, 0));
        }
    }
}
