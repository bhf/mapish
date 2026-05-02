package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.serializers.ShortSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ShortMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private Map<Short, Short> hashMap;
    private OffHeapMap<Short, Short> offHeapMap;

    private Short[] keys;
    private Short[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Short[size];
        values = new Short[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (short) i;
            values[i] = (short) i;
        }

        hashMap = new HashMap<>(size);
        offHeapMap = new OffHeapMap<>(new ShortSerializer(), new ShortSerializer(), size);

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
        Map<Short, Short> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Short, Short> map = new OffHeapMap<>(new ShortSerializer(), new ShortSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Short javaHashMap_get() {
        Short result = null;
        for (int i = 0; i < size; i++) {
            result = hashMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Short offHeapMap_get() {
        Short result = null;
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
