package org.apache.flume.node;

import com.google.common.collect.ImmutableList;
import org.apache.flume.Context;
import org.apache.flume.conf.FlumeConfiguration;
import org.apache.flume.instrumentation.util.JMXPollUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationHandler implements ConfigurationHandlerMBean {

    private final String agentName;
    private Map<String ,List< Map<String,Map<String,String>>>> conf;
    private AbstractConfigurationProvider configurationProvider;
    public static final String objectName = "org.apache.flume.node:conf=handler";

    public ConfigurationHandler(String agentName, AbstractConfigurationProvider configurationProvider) {
        this.conf = new HashMap<>();
        this.agentName = agentName;
        this.configurationProvider = configurationProvider;
        JMXPollUtil.register(objectName,this);
    }

    public void setAgentConfiguration(FlumeConfiguration.AgentConfiguration agentConf){
        conf.put(agentName, ImmutableList.of(contextToMap(agentConf.getSourceContext()),
                contextToMap(agentConf.getChannelContext()),contextToMap(agentConf.getSinkContext())));
    }
    //TODO:
    public Map<String,Map<String,String>>  contextToMap(Map<String, Context> context) {
        Map<String,Map<String,String>> result = new HashMap<>();
        for (Map.Entry<String ,Context> source: context.entrySet()){
            result.put(source.getKey(),source.getValue().getParameters());
        }
        return result;
    }

    @Override
    public Map configuration() {
        return conf;
    }

    @Override
    public void refreshConfiguration() throws IOException {
        configurationProvider.refreshConfiguration();
    }
}
