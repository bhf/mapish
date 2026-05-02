package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class FloatSerializer implements Serializer<Float> {
    @Override
    public void serialize(Float obj, MemorySegment segment, long offset) {
        segment.set(ValueLayout.JAVA_FLOAT, offset, obj);
    }

    @Override
    public Float deserialize(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_FLOAT, offset);
    }

    @Override
    public long sizeBytes() {
        return 4;
    }

    @Override
    public boolean equals(Float obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        return Float.floatToIntBits(obj) == Float.floatToIntBits(segment.get(ValueLayout.JAVA_FLOAT, offset));
    }
}
