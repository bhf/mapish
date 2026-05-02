package com.bhf.mapish.serializers;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class CharacterSerializer implements Serializer<Character> {
    @Override
    public void serialize(Character obj, MemorySegment segment, long offset) {
        segment.set(ValueLayout.JAVA_CHAR, offset, obj);
    }

    @Override
    public Character deserialize(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_CHAR, offset);
    }

    @Override
    public long sizeBytes() {
        return 2;
    }

    @Override
    public boolean equals(Character obj, MemorySegment segment, long offset) {
        if (obj == null) return false;
        return obj.charValue() == segment.get(ValueLayout.JAVA_CHAR, offset);
    }
}
