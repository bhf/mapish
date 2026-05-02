package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.serializers.BooleanSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BooleanMapBenchmark {

    @Param({"2"})
    private int size;

    private Map<Boolean, Boolean> hashMap;
    private OffHeapMap<Boolean, Boolean> offHeapMap;

    private Boolean[] keys;
    private Boolean[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Boolean[size];
        values = new Boolean[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (i % 2 == 0);
            values[i] = (i % 2 == 0);
        }

        hashMap = new HashMap<>(size);
        offHeapMap = new OffHeapMap<>(new BooleanSerializer(), new BooleanSerializer(), size);

        for (int i = 0; i < size; i++) {
            hashMap.put(keys[i], values[i]);
            offHeapMap.put(keys[i], values[i]);
        }
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        // Arena.ofAuto() handles it
    }

    @Benchmark
    public void javaHashMap_put() {
        Map<Boolean, Boolean> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Boolean, Boolean> map = new OffHeapMap<>(new BooleanSerializer(), new BooleanSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Boolean javaHashMap_get() {
        Boolean result = null;
        for (int i = 0; i < size; i++) {
            result = hashMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Boolean offHeapMap_get() {
        Boolean result = null;
        for (int i = 0; i < size; i++) {
            result = offHeapMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public boolean javaHashMap_containsKey() {
        boolean result = false;
        for (int i = 0; i < size; i++) {
            result = hashMap.containsKey(keys[i]);
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
