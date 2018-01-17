/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flume.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.flume.CounterGroup;
import org.apache.flume.conf.FlumeConfiguration;
import org.apache.flume.lifecycle.LifecycleAware;
import org.apache.flume.lifecycle.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class PollingPropertiesFileConfigurationProvider
    extends PropertiesFileConfigurationProvider
    implements LifecycleAware {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PollingPropertiesFileConfigurationProvider.class);
  private static final String DEFAULT_PROPERTIES_IMPLEMENTATION = "java.util.Properties";

  private final File file;
  private final int interval;
  private final CounterGroup counterGroup;
  private LifecycleState lifecycleState;

  private ScheduledExecutorService executorService;

  public PollingPropertiesFileConfigurationProvider(String agentName,
      File file, EventBus eventBus, int interval) {
    super(agentName,file,eventBus);
    this.file = file;
    this.interval = interval;
    counterGroup = new CounterGroup();
    lifecycleState = LifecycleState.IDLE;
  }

  @Override
  public void start() {
    if(interval == -1){
        LOGGER.info("Once configuration");
        eventBus.post(getConfiguration());
        lifecycleState = LifecycleState.STOP;
        return;
    }
    LOGGER.info("Configuration provider starting");

    Preconditions.checkState(file != null,
        "The parameter file must not be null");

    executorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("conf-file-poller-%d")
                .build());

    FileWatcherRunnable fileWatcherRunnable =
        new FileWatcherRunnable(file, counterGroup);

    executorService.scheduleWithFixedDelay(fileWatcherRunnable, 0, interval,
        TimeUnit.SECONDS);

    lifecycleState = LifecycleState.START;

    LOGGER.debug("Configuration provider started");
  }

  @Override
  public void stop() {
    LOGGER.info("Configuration provider stopping");
    if(interval == -1){

    }
    executorService.shutdown();
    try {
      while (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
        LOGGER.debug("Waiting for file watcher to terminate");
      }
    } catch (InterruptedException e) {
      LOGGER.debug("Interrupted while waiting for file watcher to terminate");
      Thread.currentThread().interrupt();
    }
    lifecycleState = LifecycleState.STOP;
    LOGGER.debug("Configuration provider stopped");
  }

  @Override
  public synchronized  LifecycleState getLifecycleState() {
    return lifecycleState;
  }


  @Override
  public String toString() {
    return "{ file:" + file + " counterGroup:" + counterGroup + "  provider:"
        + getClass().getCanonicalName() + " agentName:" + getAgentName() + " }";
  }


  public class FileWatcherRunnable implements Runnable {

    private final File file;
    private final CounterGroup counterGroup;

    private long lastChange;

    public FileWatcherRunnable(File file, CounterGroup counterGroup) {
      super();
      this.file = file;
      this.counterGroup = counterGroup;
      this.lastChange = 0L;
    }

    @Override
    public void run() {
      LOGGER.debug("Checking file:{} for changes", file);

      counterGroup.incrementAndGet("file.checks");

      long lastModified = file.lastModified();

      if (lastModified > lastChange) {
        LOGGER.info("Reloading configuration file:{}", file);

        counterGroup.incrementAndGet("file.loads");

        lastChange = lastModified;

        try {
          refreshConfiguration();
        } catch (Exception e) {
          LOGGER.error("Failed to load configuration data. Exception follows.",
              e);
        } catch (NoClassDefFoundError e) {
          LOGGER.error("Failed to start agent because dependencies were not " +
              "found in classpath. Error follows.", e);
        } catch (Throwable t) {
          // caught because the caller does not handle or log Throwables
          LOGGER.error("Unhandled error", t);
        }
      }
    }
  }

}
