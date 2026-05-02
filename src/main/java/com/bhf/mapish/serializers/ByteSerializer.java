package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ByteSerializer implements Serializer<Byte> {
    @Override
    public void serialize(Byte obj, MemorySegment segment, long offset) {
        segment.set(ValueLayout.JAVA_BYTE, offset, obj);
    }

    @Override
    public Byte deserialize(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_BYTE, offset);
    }

    @Override
    public long sizeBytes() {
        return 1;
    }

    @Override
    public boolean equals(Byte obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        return obj.byteValue() == segment.get(ValueLayout.JAVA_BYTE, offset);
    }
}
