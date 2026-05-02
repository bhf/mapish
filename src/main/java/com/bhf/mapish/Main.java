package com.bhf.mapish;

import com.bhf.mapish.serializers.*;


public class Main {
    static void main() {

        // Provide a large enough initial capacity to prevent resizing of giant contiguous off-heap arrays
        OffHeapMap<String, String> map = new OffHeapMap<>(new StringSerializer(1000), new StringSerializer(1000), 2_000_000);

        long start = System.currentTimeMillis();
        for(int i=0; i<10; i++){
            put1M(map);
            long now = System.currentTimeMillis();
            long dur = now-start;
            System.out.println("Dur="+dur);
            map.clear();
            start = System.currentTimeMillis();
        }



    }

    private static void put1M(OffHeapMap<String, String> map) {
        for (int i = 1; i <= 1000000; i++) {
            map.put("key-"+i, "value-"+i);
        }
    }
}
