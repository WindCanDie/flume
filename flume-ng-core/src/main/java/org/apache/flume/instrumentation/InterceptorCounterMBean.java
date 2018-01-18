package org.apache.flume.instrumentation;

public interface InterceptorCounterMBean {
    String getType();

    long getEventFilterCount();

    long getEventPassCount();
}
