package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.FloatSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class FloatMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private OakMap<Float, Float> oakMap;
    private OffHeapMap<Float, Float> offHeapMap;

    private Float[] keys;
    private Float[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Float[size];
        values = new Float[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (float) i;
            values[i] = (float) i;
        }

        oakMap = new OakMapBuilder<>(OakAdapters.FLOAT_COMPARATOR, OakAdapters.FLOAT_SERIALIZER, OakAdapters.FLOAT_SERIALIZER, Float.NEGATIVE_INFINITY)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new FloatSerializer(), new FloatSerializer(), size);

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
        try (OakMap<Float, Float> oak = new OakMapBuilder<>(OakAdapters.FLOAT_COMPARATOR, OakAdapters.FLOAT_SERIALIZER, OakAdapters.FLOAT_SERIALIZER, Float.NEGATIVE_INFINITY)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Float, Float> map = new OffHeapMap<>(new FloatSerializer(), new FloatSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Float oakMap_get() {
        Float result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Float offHeapMap_get() {
        Float result = null;
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
