package org.apache.flume.instrumentation;

import org.apache.commons.lang.ArrayUtils;

public class InterceptorCounter extends MonitoredCounterGroup implements InterceptorCounterMBean {
    public static final String EVENT_FILTER_COUNT = "interceptor.event.filter.count";
    public static final String EVENT_PASS_COUNT = "interceptor.event.pass.count";

    private static final String[] ATTRIBUTES = {EVENT_FILTER_COUNT, EVENT_PASS_COUNT};

    public InterceptorCounter(String name) {
        super(MonitoredCounterGroup.Type.INTERCEPTOR, name, ATTRIBUTES);
    }

    protected InterceptorCounter(String name, String[] attr) {
        super(MonitoredCounterGroup.Type.INTERCEPTOR, name, (String[]) ArrayUtils.addAll(attr, ATTRIBUTES));
    }

    @Override
    public long getEventFilterCount() {
        return get(EVENT_FILTER_COUNT);
    }

    public long addToEventFilterCount(long delta) {
        return addAndGet(EVENT_FILTER_COUNT, delta);
    }

    @Override
    public long getEventPassCount() {
        return get(EVENT_PASS_COUNT);
    }

    public long addToEventPassCount(long delta) {
        return addAndGet(EVENT_PASS_COUNT, delta);
    }

}
