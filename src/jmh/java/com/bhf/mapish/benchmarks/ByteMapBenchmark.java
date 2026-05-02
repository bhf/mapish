package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.serializers.ByteSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ByteMapBenchmark {

    @Param({"100"})
    private int size;

    private Map<Byte, Byte> hashMap;
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

        hashMap = new HashMap<>(size);
        offHeapMap = new OffHeapMap<>(new ByteSerializer(), new ByteSerializer(), size);

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
        Map<Byte, Byte> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
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
    public Byte javaHashMap_get() {
        Byte result = null;
        for (int i = 0; i < size; i++) {
            result = hashMap.get(keys[i]);
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
