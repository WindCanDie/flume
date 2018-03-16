package org.apache.flume.instrumentation.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JVMDumpUtil {
    public static MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();

    public static Map<String, Map<String, String>> getJVMDump() {
        Map<String, Map<String, String>> result = new HashMap<>();

        MemoryUsage heapUsage = memorymbean.getHeapMemoryUsage();
        Map<String, String> heapMemory = new HashMap<>();
        heapMemory.put("init", String.valueOf(heapUsage.getInit()));
        heapMemory.put("max", String.valueOf(heapUsage.getMax()));
        heapMemory.put("use", String.valueOf(heapUsage.getUsed()));
        result.put("heap", heapMemory);

        MemoryUsage nonheapUsage = memorymbean.getNonHeapMemoryUsage();
        Map<String, String> nonheapMemory = new HashMap<>();
        nonheapMemory.put("init", String.valueOf(nonheapUsage.getInit()));
        nonheapMemory.put("max", String.valueOf(nonheapUsage.getMax()));
        nonheapMemory.put("use", String.valueOf(nonheapUsage.getUsed()));
        result.put("nonheap", nonheapMemory);

        List<GarbageCollectorMXBean> gcmList = ManagementFactory.getGarbageCollectorMXBeans();
        for (int i = 0; i < gcmList.size(); i++) {
            Map<String, String> gcMeg = new HashMap<>();
            GarbageCollectorMXBean gcm = gcmList.get(i);
            gcMeg.put("name", gcm.getName());
            gcMeg.put("count", String.valueOf(gcm.getCollectionCount()));
            gcMeg.put("time", String.valueOf(gcm.getCollectionTime()));
            gcMeg.put("poolnames", Arrays.toString(gcm.getMemoryPoolNames()));
            result.put("gc" + i, gcMeg);
        }


        return result;
    }
}
