/*
 * Copyright 2015 Attribyte, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.attribyte.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import org.attribyte.api.InitializationException;
import org.attribyte.util.InitUtil;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the configuration and lifecycle of metrics reporters.
 */
public class Reporting {

   /**
    * Creates an instance that creates and configures metrics reporters.
    * @param prefix The prefix applied to property names.
    * @param props The properties.
    * @param registry The metrics registry.
    * @throws Exception on initialization error.
    */
   public Reporting(final String prefix, final Properties props,
                    final MetricRegistry registry) throws Exception {
      this(prefix, props, registry, null);
   }


   /**
    * Creates an instance that creates and configures metrics reporters with
    * filtering applied to the registry.
    * @param prefix The prefix applied to property names.
    * @param props The properties.
    * @param registry The metrics registry.
    * @param filter A metrics filter. May be <code>null</code>.
    * @throws Exception on initialization error.
    */
   public Reporting(final String prefix, final Properties props,
                    final MetricRegistry registry, final MetricFilter filter) throws Exception {

      Map<String, Properties> reporterProperties = new InitUtil(prefix, props, false).split();

      for(String name : reporterProperties.keySet()) {
         Properties currProps = reporterProperties.get(name);
         InitUtil reporterInit = new InitUtil("", currProps, false);
         Reporter reporter = (Reporter)reporterInit.initClass("class", Reporter.class);
         if(reporter != null) {
            reporter.init(name, reporterInit.getProperties(), registry, filter);
            reporters.add(reporter);
         } else {
            throw new InitializationException("The 'class' must be specified for metrics reporter, '" + name + "'");
         }
      }
   }

   /**
    * Starts all reporters.
    * @throws Exception on start error.
    */
   public int start() throws Exception {
      if(isStarted.compareAndSet(false, true)) {
         try {
            for(Reporter reporter : reporters) {
               reporter.start();
            }
         } catch(Exception e) {
            stop();
            throw e;
         }
      }
      return reporters.size();
   }

   /**
    * Stops all reporting.
    */
   public void stop() {
      if(isStarted.compareAndSet(true, false)) {
         for(Reporter reporter : reporters) {
            reporter.stop();
         }
      }
   }

   /**
    * Is reporting running?
    */
   public boolean isRunning() {
      return isStarted.get();
   }


   /**
    * All initialized reporters.
    */
   private List<Reporter> reporters = Lists.newArrayListWithExpectedSize(8);

   /**
    * Ensure started once.
    */
   private final AtomicBoolean isStarted = new AtomicBoolean(false);
}