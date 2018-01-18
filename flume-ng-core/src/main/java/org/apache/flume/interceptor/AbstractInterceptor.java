package org.apache.flume.interceptor;

import org.apache.flume.instrumentation.InterceptorCounter;

public abstract class AbstractInterceptor implements Interceptor {

    public static final String NAME_CONFG = "name";

    protected InterceptorCounter interceptorCounter;

    public abstract String getName();

    public AbstractInterceptor() {
    }

    @Override
    public void initialize() {
        this.interceptorCounter = new InterceptorCounter(getName());
        interceptorCounter.start();
    }

    public InterceptorCounter getInterceptorCounter() {
        return interceptorCounter;
    }

    @Override
    public void close() {
        interceptorCounter.stop();
    }
}
