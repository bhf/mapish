package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class BooleanSerializer implements Serializer<Boolean> {
    @Override
    public void serialize(Boolean obj, MemorySegment segment, long offset) {
        segment.set(ValueLayout.JAVA_BYTE, offset, (byte) (obj ? 1 : 0));
    }

    @Override
    public Boolean deserialize(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_BYTE, offset) != 0;
    }

    @Override
    public long sizeBytes() {
        return 1;
    }

    @Override
    public boolean equals(Boolean obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        boolean val = segment.get(ValueLayout.JAVA_BYTE, offset) != 0;
        return obj.booleanValue() == val;
    }
}
