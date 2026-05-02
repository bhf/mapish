package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ShortSerializer implements Serializer<Short> {
    @Override
    public void serialize(Short obj, MemorySegment segment, long offset) {
        segment.set(ValueLayout.JAVA_SHORT, offset, obj);
    }

    @Override
    public Short deserialize(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_SHORT, offset);
    }

    @Override
    public long sizeBytes() {
        return 2;
    }

    @Override
    public boolean equals(Short obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        return obj.shortValue() == segment.get(ValueLayout.JAVA_SHORT, offset);
    }
}
