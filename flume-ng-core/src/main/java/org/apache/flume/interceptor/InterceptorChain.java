/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.flume.interceptor;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.List;

import org.apache.flume.Event;
import com.google.common.collect.Lists;

/**
 * Implementation of Interceptor that calls a list of other Interceptors
 * serially.
 */
public class InterceptorChain implements Interceptor {

  // list of interceptors that will be traversed, in order
  private List<Interceptor> interceptors;

  public InterceptorChain() {
    interceptors = Lists.newLinkedList();
  }

  public void setInterceptors(List<Interceptor> interceptors) {
    this.interceptors = interceptors;
  }

  @Override
  public Event intercept(Event event) {
    for (Interceptor interceptor : interceptors) {
      if (event == null) {
        return null;
      }
      event = interceptor.intercept(event);
      if (event != null){
          ((AbstractInterceptor)interceptor).getInterceptorCounter().addToEventPassCount(1);
      }else {
          ((AbstractInterceptor)interceptor).getInterceptorCounter().addToEventFilterCount(1);
      }
    }
    return event;
  }

  @Override
  public List<Event> intercept(List<Event> events) {
    for (Interceptor interceptor : interceptors) {
      if (events.isEmpty()) {
        return events;
      }
      long afterSize = events.size();
      events = interceptor.intercept(events);
      Preconditions.checkNotNull(events,
          "Event list returned null from interceptor %s", interceptor);
      ((AbstractInterceptor)interceptor).getInterceptorCounter().addToEventPassCount(events.size());
      ((AbstractInterceptor)interceptor).getInterceptorCounter().addToEventFilterCount(afterSize - events.size());
    }
    return events;
  }

  @Override
  public void initialize() {
    Iterator<Interceptor> iter = interceptors.iterator();
    while (iter.hasNext()) {
      Interceptor interceptor = iter.next();
      interceptor.initialize();
    }
  }

  @Override
  public void close() {
    Iterator<Interceptor> iter = interceptors.iterator();
    while (iter.hasNext()) {
      Interceptor interceptor = iter.next();
      interceptor.close();
    }
  }

}
