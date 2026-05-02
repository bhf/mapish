package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class DoubleSerializer implements Serializer<Double> {
    @Override
    public void serialize(Double obj, MemorySegment segment, long offset) {
        segment.set(ValueLayout.JAVA_DOUBLE, offset, obj);
    }

    @Override
    public Double deserialize(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_DOUBLE, offset);
    }

    @Override
    public long sizeBytes() {
        return 8;
    }

    @Override
    public boolean equals(Double obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        return Double.doubleToLongBits(obj) == Double.doubleToLongBits(segment.get(ValueLayout.JAVA_DOUBLE, offset));
    }
}
