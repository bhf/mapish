# Off-Heap Mapish

[![Build and Test](https://github.com/bhf/mapish/actions/workflows/build.yml/badge.svg)](https://github.com/bhf/mapish/actions/workflows/build.yml)

A high-performance, single-threaded, off-heap hash map implementation in Java that fully implements the `java.util.Map` contract. This project utilizes Java's Foreign Function & Memory (FFM) API to manage data structures directly in native memory, bypassing the Java Garbage Collector (GC) for the underlying table nodes.

## Table of Contents
- [Memory Lifecycle & AutoCloseable](#memory-lifecycle--autocloseable)
- [Implementation Details](#implementation-details)
  - [Architecture & Design](#architecture--design)
- [Performance Optimizations](#performance-optimizations)
- [Testing Strategy](#testing-strategy)
- [Running the Project](#running-the-project)

---

## Memory Lifecycle & AutoCloseable

By default, an `OffHeapMap` is created using `Arena.ofAuto()`. This delegates the cleanup of native memory to the Java Garbage Collector, allowing it to behave somewhat like a standard on-heap object and safely avoid accidental use-after-free errors. 

```java
OffHeapMap<String, String> map = new OffHeapMap<>(new StringSerializer(100), new StringSerializer(100), 2_000_000);
map.put("key", "value");
// No explicit closing required; memory is freed when the map is garbage collected.
```

However, if you are allocating very large maps and need strict, deterministic control over the native memory release (to prevent exhausting system memory resources before the GC runs), you can instantiate a confined map:

```java
try (OffHeapMap<String, String> map = OffHeapMap.createConfined(new StringSerializer(100), new StringSerializer(100), 2_000_000)) {
    map.put("key", "value");
    // Work with the map
} // The native memory is immediately unmapped and released to the OS here
```

Confined maps implement `AutoCloseable` and immediately drop the underlying memory allocations upon closing. Any attempt to read from or write to the map after it has been closed will safely throw an `IllegalStateException`.

[Back to top](#off-heap-mapish)

---

## Implementation Details

Instead of relying on arrays of Java Object references (which live on the heap and are tracked by the GC), `OffHeapMap` allocates a single, contiguous off-heap `MemorySegment` to store all keys, values, and structural metadata.

### Architecture & Design
* **FFM API:** Memory is managed directly using `Arena.ofAuto()` and `MemorySegment`. 
* **Generic Serializers:** Because the map stores raw bytes, generic `<K, V>` types are supported via a custom `Serializer<T>` interface. The map includes built-in serializers for primitives and common types (`String`, `Integer`, `Long`, `Double`, `Float`, `Short`, `Byte`, `Boolean`, `Character`) encapsulated in the `com.bhf.mapish.serializers` package.
* **Collision Resolution:** Instead of the separate chaining and Red-Black trees used by the standard `java.util.HashMap`, this map uses **Open Addressing with Linear Probing**. This avoids complex pointer management, significantly reduces memory overhead, and maximizes CPU cache locality by scanning adjacent memory blocks.
* **Memory Layout:** Each entry in the map is stored sequentially in a fixed-size chunk calculated dynamically at startup:
  `[1 byte Status] [Padding] [4 byte Cached Hash] [Key Bytes] [Value Bytes]`

[Back to top](#off-heap-mapish)

---

## Performance Optimizations

To mitigate the heavy costs of serializing and deserializing data back and forth between the JVM heap and native memory, we implemented several key performance tricks:

1. **Hash Caching:** The 32-bit `hashCode()` of every key is cached locally within the entry metadata layout. When the map linearly probes through a collision chain, it checks for integer equivalence first. This allows the map to skip complex deserialization on hash mismatches entirely.
2. **Zero-Deserialization Comparisons:** The `Serializer` interface implements a specialized `equals(T obj, MemorySegment segment, long offset)` method. For types like `String`, instead of allocating a new `byte[]` and extracting a UTF-8 `String` onto the heap just to check equality, we utilize `MemorySegment.mismatch()` to execute blazing-fast binary sequence comparisons directly against the hardware vector logic.
3. **Pre-sizing / Capacity Allocation:** Re-allocating massive contiguous memory segments off-heap can be incredibly expensive and trigger the JVM's `OutOfMemoryError` on native buffer reservations. Pre-sizing the map via the constructor avoids O(N) array copying and allows million-entry insertions instantly.

[Back to top](#off-heap-mapish)

---

## Testing Strategy

Validating custom data structures requires rigorous boundaries testing. 

* **Contract Verification:** We utilize **Google's `guava-testlib`** via `MapTestSuiteBuilder`. This dynamically generates hundreds of permutations and structural edge-case tests to strictly verify that `OffHeapMap` conforms 100% perfectly to the standard `java.util.Map` interface contracts (handling null boundaries, `entrySet()` iterator removals, deep `hashCode()` constraints, etc.).
* **Benchmarking:** Throughput is measured using **Java Microbenchmark Harness (JMH)**. The benchmark suite (`src/jmh/java/com/bhf/mapish/benchmarks/`) includes separate, encapsulated benchmarks for each primitive serializer (String, Integer, Double, etc.). It compares operations (`put`, `get`, `containsKey`) against `java.util.HashMap` over 100 and 10,000 element scale runs to observe scaling thresholds, memory boundaries, and throughput operations per millisecond.
* **Component Testing:** Comprehensive JUnit tests ensure stability across the isolated serializers and `AutoCloseable` memory lifecycles.

[Back to top](#off-heap-mapish)

---

## Running the Project

**Run the Guava Unit Test Suite:**
```bash
./gradlew test
```

**Run the JMH Benchmarks:**
```bash
./gradlew jmh
```
*Note: The entire JMH test suite is configured to output metrics directly in JSON format (found in `build/results/jmh/results.json`) making it easy for machine analysis and visualization pipelines.*

[Back to top](#off-heap-mapish)
