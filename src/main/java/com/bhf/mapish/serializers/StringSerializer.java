package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public class StringSerializer implements Serializer<String> {
    
    private final int maxBytes;
    private final long sizeBytes;

    public StringSerializer(int maxBytes) {
        this.maxBytes = maxBytes;
        // 4 bytes to store the length of the string, plus the max bytes for the characters
        this.sizeBytes = maxBytes + 4;
    }

    @Override
    public void serialize(String obj, MemorySegment segment, long offset) {
        byte[] bytes = obj.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxBytes) {
            throw new IllegalArgumentException("String byte length (" + bytes.length + ") exceeds maximum (" + maxBytes + ")");
        }
        
        // Write the length
        segment.set(ValueLayout.JAVA_INT, offset, bytes.length);
        
        // Write the string bytes
        MemorySegment.copy(MemorySegment.ofArray(bytes), 0, segment, offset + 4, bytes.length);
    }

    @Override
    public String deserialize(MemorySegment segment, long offset) {
        int length = segment.get(ValueLayout.JAVA_INT, offset);
        if (length < 0 || length > maxBytes) {
            throw new IllegalStateException("Corrupted string length or uninitialized memory read");
        }
        
        if (length == 0) return "";
        
        byte[] bytes = new byte[length];
        MemorySegment.copy(segment, offset + 4, MemorySegment.ofArray(bytes), 0, length);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public long sizeBytes() {
        return sizeBytes;
    }

    @Override
    public boolean equals(String obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        
        int length = segment.get(ValueLayout.JAVA_INT, offset);
        byte[] bytes = obj.getBytes(StandardCharsets.UTF_8);
        if (length != bytes.length) return false;
        
        if (length == 0) return true;
        
        MemorySegment objSegment = MemorySegment.ofArray(bytes);
        return MemorySegment.mismatch(segment, offset + 4, offset + 4 + length, objSegment, 0, length) == -1;
    }
}
