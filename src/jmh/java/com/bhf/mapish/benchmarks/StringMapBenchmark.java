package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.StringSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class StringMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private OakMap<String, String> oakMap;
    private OffHeapMap<String, String> offHeapMap;

    private String[] keys;
    private String[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new String[size];
        values = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = "Key_" + i;
            values[i] = "Value_" + i;
        }

        oakMap = new OakMapBuilder<>(OakAdapters.STRING_COMPARATOR, OakAdapters.STRING_SERIALIZER, OakAdapters.STRING_SERIALIZER, "")
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new StringSerializer(64), new StringSerializer(64), size);

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
        try (OakMap<String, String> oak = new OakMapBuilder<>(OakAdapters.STRING_COMPARATOR, OakAdapters.STRING_SERIALIZER, OakAdapters.STRING_SERIALIZER, "")
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<String, String> map = new OffHeapMap<>(new StringSerializer(64), new StringSerializer(64), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public String oakMap_get() {
        String result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public String offHeapMap_get() {
        String result = null;
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
