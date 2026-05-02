package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class IntegerSerializer implements Serializer<Integer> {
    @Override
    public void serialize(Integer obj, MemorySegment segment, long offset) {
        segment.set(ValueLayout.JAVA_INT, offset, obj);
    }

    @Override
    public Integer deserialize(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_INT, offset);
    }

    @Override
    public long sizeBytes() {
        return 4;
    }

    @Override
    public boolean equals(Integer obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        return obj.intValue() == segment.get(ValueLayout.JAVA_INT, offset);
    }
}
