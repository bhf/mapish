package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class LongSerializer implements Serializer<Long> {
    @Override
    public void serialize(Long obj, MemorySegment segment, long offset) {
        segment.set(ValueLayout.JAVA_LONG, offset, obj);
    }

    @Override
    public Long deserialize(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_LONG, offset);
    }

    @Override
    public long sizeBytes() {
        return 8;
    }

    @Override
    public boolean equals(Long obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        return obj.longValue() == segment.get(ValueLayout.JAVA_LONG, offset);
    }
}
