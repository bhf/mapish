package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;

public interface Serializer<T> {
    void serialize(T obj, MemorySegment segment, long offset);
    T deserialize(MemorySegment segment, long offset);
    long sizeBytes();
    boolean equals(T obj, MemorySegment segment, long offset);
}
