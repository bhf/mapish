package com.bhf.mapish.benchmarks.oak;

import com.yahoo.oak.OakComparator;
import com.yahoo.oak.OakScopedReadBuffer;
import com.yahoo.oak.OakScopedWriteBuffer;
import com.yahoo.oak.OakSerializer;

import java.nio.charset.StandardCharsets;

/**
 * Provides OakSerializer and OakComparator implementations for all primitive wrapper types
 * and String, used by JMH benchmarks to construct OakMap instances.
 */
public final class OakAdapters {
    private OakAdapters() {}

    /** Calculates a reasonable off-heap memory capacity for OakMap given the number of entries. */
    public static long oakCapacity(int size) {
        return Math.max((long) size * 512, 4L * 1024 * 1024);
    }

    // ===== Integer =====

    public static final OakSerializer<Integer> INTEGER_SERIALIZER = new OakSerializer<Integer>() {
        @Override public void serialize(Integer data, OakScopedWriteBuffer buf) { buf.putInt(0, data); }
        @Override public Integer deserialize(OakScopedReadBuffer buf) { return buf.getInt(0); }
        @Override public int calculateSize(Integer data) { return Integer.BYTES; }
        @Override public int calculateHash(Integer data) { return data.hashCode(); }
    };

    public static final OakComparator<Integer> INTEGER_COMPARATOR = new OakComparator<Integer>() {
        @Override public int compareKeys(Integer k1, Integer k2) { return Integer.compare(k1, k2); }
        @Override public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            return Integer.compare(b1.getInt(0), b2.getInt(0));
        }
        @Override public int compareKeyAndSerializedKey(Integer k, OakScopedReadBuffer b) {
            return Integer.compare(k, b.getInt(0));
        }
    };

    // ===== Long =====

    public static final OakSerializer<Long> LONG_SERIALIZER = new OakSerializer<Long>() {
        @Override public void serialize(Long data, OakScopedWriteBuffer buf) { buf.putLong(0, data); }
        @Override public Long deserialize(OakScopedReadBuffer buf) { return buf.getLong(0); }
        @Override public int calculateSize(Long data) { return Long.BYTES; }
        @Override public int calculateHash(Long data) { return data.hashCode(); }
    };

    public static final OakComparator<Long> LONG_COMPARATOR = new OakComparator<Long>() {
        @Override public int compareKeys(Long k1, Long k2) { return Long.compare(k1, k2); }
        @Override public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            return Long.compare(b1.getLong(0), b2.getLong(0));
        }
        @Override public int compareKeyAndSerializedKey(Long k, OakScopedReadBuffer b) {
            return Long.compare(k, b.getLong(0));
        }
    };

    // ===== Double =====

    public static final OakSerializer<Double> DOUBLE_SERIALIZER = new OakSerializer<Double>() {
        @Override public void serialize(Double data, OakScopedWriteBuffer buf) { buf.putDouble(0, data); }
        @Override public Double deserialize(OakScopedReadBuffer buf) { return buf.getDouble(0); }
        @Override public int calculateSize(Double data) { return Double.BYTES; }
        @Override public int calculateHash(Double data) { return data.hashCode(); }
    };

    public static final OakComparator<Double> DOUBLE_COMPARATOR = new OakComparator<Double>() {
        @Override public int compareKeys(Double k1, Double k2) { return Double.compare(k1, k2); }
        @Override public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            return Double.compare(b1.getDouble(0), b2.getDouble(0));
        }
        @Override public int compareKeyAndSerializedKey(Double k, OakScopedReadBuffer b) {
            return Double.compare(k, b.getDouble(0));
        }
    };

    // ===== Float =====

    public static final OakSerializer<Float> FLOAT_SERIALIZER = new OakSerializer<Float>() {
        @Override public void serialize(Float data, OakScopedWriteBuffer buf) { buf.putFloat(0, data); }
        @Override public Float deserialize(OakScopedReadBuffer buf) { return buf.getFloat(0); }
        @Override public int calculateSize(Float data) { return Float.BYTES; }
        @Override public int calculateHash(Float data) { return data.hashCode(); }
    };

    public static final OakComparator<Float> FLOAT_COMPARATOR = new OakComparator<Float>() {
        @Override public int compareKeys(Float k1, Float k2) { return Float.compare(k1, k2); }
        @Override public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            return Float.compare(b1.getFloat(0), b2.getFloat(0));
        }
        @Override public int compareKeyAndSerializedKey(Float k, OakScopedReadBuffer b) {
            return Float.compare(k, b.getFloat(0));
        }
    };

    // ===== Short =====

    public static final OakSerializer<Short> SHORT_SERIALIZER = new OakSerializer<Short>() {
        @Override public void serialize(Short data, OakScopedWriteBuffer buf) { buf.putShort(0, data); }
        @Override public Short deserialize(OakScopedReadBuffer buf) { return buf.getShort(0); }
        @Override public int calculateSize(Short data) { return Short.BYTES; }
        @Override public int calculateHash(Short data) { return data.hashCode(); }
    };

    public static final OakComparator<Short> SHORT_COMPARATOR = new OakComparator<Short>() {
        @Override public int compareKeys(Short k1, Short k2) { return Short.compare(k1, k2); }
        @Override public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            return Short.compare(b1.getShort(0), b2.getShort(0));
        }
        @Override public int compareKeyAndSerializedKey(Short k, OakScopedReadBuffer b) {
            return Short.compare(k, b.getShort(0));
        }
    };

    // ===== Byte =====

    public static final OakSerializer<Byte> BYTE_SERIALIZER = new OakSerializer<Byte>() {
        @Override public void serialize(Byte data, OakScopedWriteBuffer buf) { buf.put(0, data); }
        @Override public Byte deserialize(OakScopedReadBuffer buf) { return buf.get(0); }
        @Override public int calculateSize(Byte data) { return Byte.BYTES; }
        @Override public int calculateHash(Byte data) { return data.hashCode(); }
    };

    public static final OakComparator<Byte> BYTE_COMPARATOR = new OakComparator<Byte>() {
        @Override public int compareKeys(Byte k1, Byte k2) { return Byte.compare(k1, k2); }
        @Override public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            return Byte.compare(b1.get(0), b2.get(0));
        }
        @Override public int compareKeyAndSerializedKey(Byte k, OakScopedReadBuffer b) {
            return Byte.compare(k, b.get(0));
        }
    };

    // ===== Boolean =====

    public static final OakSerializer<Boolean> BOOLEAN_SERIALIZER = new OakSerializer<Boolean>() {
        @Override public void serialize(Boolean data, OakScopedWriteBuffer buf) {
            buf.put(0, (byte) (data ? 1 : 0));
        }
        @Override public Boolean deserialize(OakScopedReadBuffer buf) { return buf.get(0) != 0; }
        @Override public int calculateSize(Boolean data) { return Byte.BYTES; }
        @Override public int calculateHash(Boolean data) { return data.hashCode(); }
    };

    public static final OakComparator<Boolean> BOOLEAN_COMPARATOR = new OakComparator<Boolean>() {
        @Override public int compareKeys(Boolean k1, Boolean k2) { return Boolean.compare(k1, k2); }
        @Override public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            return Byte.compare(b1.get(0), b2.get(0));
        }
        @Override public int compareKeyAndSerializedKey(Boolean k, OakScopedReadBuffer b) {
            return Byte.compare((byte) (k ? 1 : 0), b.get(0));
        }
    };

    // ===== Character =====

    public static final OakSerializer<Character> CHARACTER_SERIALIZER = new OakSerializer<Character>() {
        @Override public void serialize(Character data, OakScopedWriteBuffer buf) { buf.putChar(0, data); }
        @Override public Character deserialize(OakScopedReadBuffer buf) { return buf.getChar(0); }
        @Override public int calculateSize(Character data) { return Character.BYTES; }
        @Override public int calculateHash(Character data) { return data.hashCode(); }
    };

    public static final OakComparator<Character> CHARACTER_COMPARATOR = new OakComparator<Character>() {
        @Override public int compareKeys(Character k1, Character k2) { return Character.compare(k1, k2); }
        @Override public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            return Character.compare(b1.getChar(0), b2.getChar(0));
        }
        @Override public int compareKeyAndSerializedKey(Character k, OakScopedReadBuffer b) {
            return Character.compare(k, b.getChar(0));
        }
    };

    // ===== String =====

    public static final OakSerializer<String> STRING_SERIALIZER = new OakSerializer<String>() {
        @Override
        public void serialize(String data, OakScopedWriteBuffer buf) {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            buf.putInt(0, bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                buf.put(Integer.BYTES + i, bytes[i]);
            }
        }

        @Override
        public String deserialize(OakScopedReadBuffer buf) {
            int len = buf.getInt(0);
            byte[] bytes = new byte[len];
            for (int i = 0; i < len; i++) {
                bytes[i] = buf.get(Integer.BYTES + i);
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }

        @Override
        public int calculateSize(String data) {
            return Integer.BYTES + data.getBytes(StandardCharsets.UTF_8).length;
        }

        @Override
        public int calculateHash(String data) { return data.hashCode(); }
    };

    public static final OakComparator<String> STRING_COMPARATOR = new OakComparator<String>() {
        @Override
        public int compareKeys(String k1, String k2) { return k1.compareTo(k2); }

        @Override
        public int compareSerializedKeys(OakScopedReadBuffer b1, OakScopedReadBuffer b2) {
            int len1 = b1.getInt(0);
            int len2 = b2.getInt(0);
            int minLen = Math.min(len1, len2);
            for (int i = 0; i < minLen; i++) {
                int cmp = Byte.compare(b1.get(Integer.BYTES + i), b2.get(Integer.BYTES + i));
                if (cmp != 0) return cmp;
            }
            return Integer.compare(len1, len2);
        }

        @Override
        public int compareKeyAndSerializedKey(String k, OakScopedReadBuffer b) {
            byte[] kBytes = k.getBytes(StandardCharsets.UTF_8);
            int len = b.getInt(0);
            int minLen = Math.min(kBytes.length, len);
            for (int i = 0; i < minLen; i++) {
                int cmp = Byte.compare(kBytes[i], b.get(Integer.BYTES + i));
                if (cmp != 0) return cmp;
            }
            return Integer.compare(kBytes.length, len);
        }
    };
}
