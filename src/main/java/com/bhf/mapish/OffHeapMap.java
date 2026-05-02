package com.bhf.mapish;

import com.bhf.mapish.serializers.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class OffHeapMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, AutoCloseable {
    
    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.5f;
    
    // Status bytes
    private static final byte EMPTY = 0;
    private static final byte FILLED = 1;
    private static final byte DELETED = 2;

    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    
    private final long entrySize;
    private final long hashOffset;
    private final long keyOffset;
    private final long valueOffset;
    
    private final Arena arena;
    private final boolean closeArena;
    private MemorySegment memorySegment;
    private int capacity;
    private int size;
    private int threshold;
    
    public OffHeapMap(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        this(keySerializer, valueSerializer, DEFAULT_CAPACITY);
    }
    
    public OffHeapMap(Serializer<K> keySerializer, Serializer<V> valueSerializer, int capacity) {
        this(keySerializer, valueSerializer, capacity, Arena.ofAuto(), false);
    }
    
    public OffHeapMap(Serializer<K> keySerializer, Serializer<V> valueSerializer, int capacity, Arena arena) {
        this(keySerializer, valueSerializer, capacity, arena, false);
    }

    protected OffHeapMap(Serializer<K> keySerializer, Serializer<V> valueSerializer, int capacity, Arena arena, boolean closeArena) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        
        long keySize = keySerializer.sizeBytes();
        long valueSize = valueSerializer.sizeBytes();
        
        // Ensure offsets are somewhat aligned (simplistic alignment)
        this.hashOffset = 4;
        this.keyOffset = 8; 
        this.valueOffset = this.keyOffset + align(keySize, 8);
        this.entrySize = this.valueOffset + align(valueSize, 8);
        
        // Find power of 2 capacity
        int cap = 1;
        while (cap < capacity) {
            cap <<= 1;
        }
        this.capacity = Math.max(DEFAULT_CAPACITY, cap);
        this.arena = arena;
        this.closeArena = closeArena;
        this.memorySegment = arena.allocate((long) this.capacity * entrySize);
        this.threshold = (int) (this.capacity * LOAD_FACTOR);
    }
    
    public static <K, V> OffHeapMap<K, V> createConfined(Serializer<K> keySerializer, Serializer<V> valueSerializer, int capacity) {
        return new OffHeapMap<>(keySerializer, valueSerializer, capacity, Arena.ofConfined(), true);
    }

    @Override
    public void close() {
        if (closeArena && arena != null && arena.scope().isAlive()) {
            arena.close();
        }
    }

    private long align(long size, long alignment) {
        return (size + alignment - 1) & ~(alignment - 1);
    }

    private int hash(Object key) {
        int h = key.hashCode();
        h ^= (h >>> 16);
        return (h & 0x7fffffff) % capacity;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return findEntryIndex(key) >= 0;
    }

    @Override
    public V get(Object key) {
        long index = findEntryIndex(key);
        if (index >= 0) {
            return valueSerializer.deserialize(memorySegment, index * entrySize + valueOffset);
        }
        return null;
    }

    private long findEntryIndex(Object key) {
        int h = hash(key);
        int index = h;
        for (int i = 0; i < capacity; i++) {
            long offset = ((long) index) * entrySize;
            byte status = memorySegment.get(ValueLayout.JAVA_BYTE, offset);
            
            if (status == EMPTY) {
                return -1;
            } else if (status == FILLED) {
                int storedHash = memorySegment.get(ValueLayout.JAVA_INT, offset + hashOffset);
                if (storedHash == h) {
                    @SuppressWarnings("unchecked")
                    K k = (K) key;
                    if (keySerializer.equals(k, memorySegment, offset + keyOffset)) {
                        return index;
                    }
                }
            }
            index = (index + 1) % capacity; // linear probing
        }
        return -1;
    }

    @Override
    public V put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException("Nulls not supported");
        if (size >= threshold) {
            resize();
        }
        return insert(memorySegment, capacity, key, value, false);
    }
    
    protected V insert(MemorySegment segment, int cap, K key, V value, boolean isResize) {
        int h = hash(key);
        int index = h % cap; 
        for (int i = 0; i < cap; i++) {
            long offset = ((long) index) * entrySize;
            byte status = segment.get(ValueLayout.JAVA_BYTE, offset);
            
            if (status == EMPTY || status == DELETED) {
                segment.set(ValueLayout.JAVA_BYTE, offset, FILLED);
                segment.set(ValueLayout.JAVA_INT, offset + hashOffset, h);
                keySerializer.serialize(key, segment, offset + keyOffset);
                valueSerializer.serialize(value, segment, offset + valueOffset);
                if (!isResize) size++;
                return null;
            } else if (status == FILLED) {
                int storedHash = segment.get(ValueLayout.JAVA_INT, offset + hashOffset);
                if (storedHash == h && keySerializer.equals(key, segment, offset + keyOffset)) {
                    V oldValue = valueSerializer.deserialize(segment, offset + valueOffset);
                    valueSerializer.serialize(value, segment, offset + valueOffset);
                    return oldValue;
                }
            }
            index = (index + 1) % cap;
        }
        return null; 
    }

    private void resize() {
        int newCapacity = capacity * 2;
        MemorySegment newSegment = arena.allocate((long) newCapacity * entrySize);
        
        for (int i = 0; i < capacity; i++) {
            long offset = ((long) i) * entrySize;
            byte status = memorySegment.get(ValueLayout.JAVA_BYTE, offset);
            if (status == FILLED) {
                K key = keySerializer.deserialize(memorySegment, offset + keyOffset);
                V value = valueSerializer.deserialize(memorySegment, offset + valueOffset);
                insert(newSegment, newCapacity, key, value, true);
            }
        }
        
        this.capacity = newCapacity;
        this.memorySegment = newSegment;
        this.threshold = (int) (capacity * LOAD_FACTOR);
    }

    @Override
    public V remove(Object key) {
        if (key == null) return null;
        
        int h = hash(key);
        int index = h;
        for (int i = 0; i < capacity; i++) {
            long offset = ((long) index) * entrySize;
            byte status = memorySegment.get(ValueLayout.JAVA_BYTE, offset);
            
            if (status == EMPTY) {
                return null;
            } else if (status == FILLED) {
                int storedHash = memorySegment.get(ValueLayout.JAVA_INT, offset + hashOffset);
                if (storedHash == h) {
                    @SuppressWarnings("unchecked")
                    K k = (K) key;
                    if (keySerializer.equals(k, memorySegment, offset + keyOffset)) {
                        V oldValue = valueSerializer.deserialize(memorySegment, offset + valueOffset);
                        memorySegment.set(ValueLayout.JAVA_BYTE, offset, DELETED);
                        size--;
                        return oldValue;
                    }
                }
            }
            index = (index + 1) % capacity;
        }
        return null;
    }

    @Override
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            long offset = ((long) i) * entrySize;
            memorySegment.set(ValueLayout.JAVA_BYTE, offset, EMPTY);
        }
        size = 0;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }
    
    private class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return OffHeapMap.this.size;
        }
        
        @Override
        public void clear() {
            OffHeapMap.this.clear();
        }
        
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            Object key = e.getKey();
            if (!OffHeapMap.this.containsKey(key)) return false;
            V val = OffHeapMap.this.get(key);
            Object v = e.getValue();
            return val == null ? v == null : val.equals(v);
        }

        @Override
        public boolean remove(Object o) {
            if (contains(o)) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                OffHeapMap.this.remove(e.getKey());
                return true;
            }
            return false;
        }
    }
    
    private class EntryIterator implements Iterator<Entry<K, V>> {
        private int currentIndex = 0;
        private int lastReturnedIndex = -1;
        private int returnedCount = 0;

        @Override
        public boolean hasNext() {
            return returnedCount < size;
        }

        @Override
        public Entry<K, V> next() {
            if (!hasNext()) throw new NoSuchElementException();
            
            while (currentIndex < capacity) {
                long offset = ((long) currentIndex) * entrySize;
                byte status = memorySegment.get(ValueLayout.JAVA_BYTE, offset);
                if (status == FILLED) {
                    lastReturnedIndex = currentIndex;
                    K key = keySerializer.deserialize(memorySegment, offset + keyOffset);
                    currentIndex++;
                    returnedCount++;
                    
                    return new Map.Entry<K, V>() {
                        @Override
                        public K getKey() { return key; }
                        @Override
                        public V getValue() {
                            return valueSerializer.deserialize(memorySegment, offset + valueOffset);
                        }
                        @Override
                        public V setValue(V newValue) {
                            if (newValue == null) throw new NullPointerException();
                            V old = valueSerializer.deserialize(memorySegment, offset + valueOffset);
                            valueSerializer.serialize(newValue, memorySegment, offset + valueOffset);
                            return old;
                        }
                        @Override
                        public boolean equals(Object o) {
                            if (!(o instanceof Map.Entry)) return false;
                            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                            K k1 = getKey(); Object k2 = e.getKey();
                            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                                V v1 = getValue(); Object v2 = e.getValue();
                                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                                    return true;
                                }
                            }
                            return false;
                        }
                        @Override
                        public int hashCode() {
                            K k = getKey(); V v = getValue();
                            return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
                        }
                        @Override
                        public String toString() {
                            return getKey() + "=" + getValue();
                        }
                    };
                }
                currentIndex++;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (lastReturnedIndex == -1) throw new IllegalStateException();
            long offset = ((long) lastReturnedIndex) * entrySize;
            memorySegment.set(ValueLayout.JAVA_BYTE, offset, DELETED);
            OffHeapMap.this.size--;
            returnedCount--;
            lastReturnedIndex = -1;
        }
    }
}
