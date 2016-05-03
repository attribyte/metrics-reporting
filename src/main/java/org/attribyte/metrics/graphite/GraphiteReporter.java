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

package org.attribyte.metrics.graphite;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.attribyte.api.InitializationException;
import org.attribyte.metrics.Reporter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.attribyte.metrics.ReporterBase;
import org.attribyte.util.InitUtil;

public class GraphiteReporter extends ReporterBase implements Reporter {

   /**
    * The graphite host ('graphite.host').
    */
   public static final String HOST_PROPERTY = "host";

   /**
    * The prefix added to metrics reported to graphite ('graphite.prefix').
    * If unspecified, the hostname is used.
    */
   public static final String PREFIX_PROPERTY = "prefix";

   /**
    * The graphite port ('graphite.port').
    * If unspecified, the default '2003' is used.
    */
   public static final String PORT_PROPERTY = "port";

   @Override
   public void init(final String name,
                    final Properties _props,
                    final MetricRegistry registry, final MetricFilter filter) throws Exception {
      if(isInit.compareAndSet(false, true)) {
         init(name, _props);
         String graphiteHost = init.getProperty(HOST_PROPERTY, "");
         if(graphiteHost.isEmpty()) {
            init.throwRequiredException(HOST_PROPERTY);
         }
         int graphitePort = init.getIntProperty(PORT_PROPERTY, 2003);

         String graphitePrefix = init.getProperty(PREFIX_PROPERTY, getHostname()).trim();
         com.codahale.metrics.graphite.GraphiteReporter.Builder builder =
                 com.codahale.metrics.graphite.GraphiteReporter
                         .forRegistry(registry)
                         .convertDurationsTo(TimeUnit.valueOf(init.getProperty(DURATION_UNIT_PROPERTY, "MILLISECONDS").toUpperCase()))
                         .convertRatesTo(TimeUnit.valueOf(init.getProperty(RATE_UNIT_PROPERTY, "SECONDS").toUpperCase()));

         if(filter != null) {
            builder.filter(filter);
         }

         if(!Strings.isNullOrEmpty(graphitePrefix)) {
            builder.prefixedWith(graphitePrefix);
         }

         InetSocketAddress addy = new InetSocketAddress(graphiteHost.trim(), graphitePort);
         Graphite graphite = new Graphite(addy);
         reporter = builder.build(graphite);
         frequencyMillis = InitUtil.millisFromTime(init.getProperty(FREQUENCY_PROPERTY, "1m"));
      }
   }

   @Override
   public void start() throws Exception {
      if(!isInit.get()) {
         throw new InitializationException("The reporter must be initialized before start!");
      }
      if(isRunning.compareAndSet(false, true)) {
         this.reporter.start(frequencyMillis, TimeUnit.MILLISECONDS);
      }
   }

   @Override
   public void stop() {
      if(isRunning.compareAndSet(true, false)) {
         this.reporter.stop();
      }
   }

   @Override
   public Map<String, Metric> getMetrics() {
      return ImmutableMap.of();
   }

   private com.codahale.metrics.graphite.GraphiteReporter reporter;
   private long frequencyMillis;
   private final AtomicBoolean isRunning = new AtomicBoolean(false);
}