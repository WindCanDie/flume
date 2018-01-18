package org.apache.flume.node;

import java.io.IOException;
import java.util.Map;

public interface ConfigurationHandlerMBean {
    Map configuration();

    void refreshConfiguration() throws IOException;
}

