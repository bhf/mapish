package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.ByteSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ByteMapBenchmark {

    @Param({"100"})
    private int size;

    private OakMap<Byte, Byte> oakMap;
    private OffHeapMap<Byte, Byte> offHeapMap;

    private Byte[] keys;
    private Byte[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Byte[size];
        values = new Byte[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (byte) i;
            values[i] = (byte) i;
        }

        oakMap = new OakMapBuilder<>(OakAdapters.BYTE_COMPARATOR, OakAdapters.BYTE_SERIALIZER, OakAdapters.BYTE_SERIALIZER, Byte.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new ByteSerializer(), new ByteSerializer(), size);

        for (int i = 0; i < size; i++) {
            oakMap.put(keys[i], values[i]);
            offHeapMap.put(keys[i], values[i]);
        }
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        if (oakMap != null) {
            oakMap.close();
        }
    }

    @Benchmark
    public void oakMap_put() {
        try (OakMap<Byte, Byte> oak = new OakMapBuilder<>(OakAdapters.BYTE_COMPARATOR, OakAdapters.BYTE_SERIALIZER, OakAdapters.BYTE_SERIALIZER, Byte.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Byte, Byte> map = new OffHeapMap<>(new ByteSerializer(), new ByteSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Byte oakMap_get() {
        Byte result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Byte offHeapMap_get() {
        Byte result = null;
        for (int i = 0; i < size; i++) {
            result = offHeapMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public boolean oakMap_containsKey() {
        boolean result = false;
        for (int i = 0; i < size; i++) {
            result = oakMap.containsKey(keys[i]);
        }
        return result;
    }

    @Benchmark
    public boolean offHeapMap_containsKey() {
        boolean result = false;
        for (int i = 0; i < size; i++) {
            result = offHeapMap.containsKey(keys[i]);
        }
        return result;
    }
}
