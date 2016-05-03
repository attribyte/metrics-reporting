/*
 * Copyright 2016 Attribyte, LLC
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

package org.attribyte.metrics.newrelic;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.attribyte.api.InitializationException;
import org.attribyte.metrics.MetricField;
import org.attribyte.metrics.Reporter;
import org.attribyte.metrics.ReporterBase;
import org.attribyte.util.InitUtil;

import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reports metrics to New Relic as custom metrics.
 */
public class NewRelicReporter extends ReporterBase implements Reporter {

   public static final String REPORT_PREFIX = "report.";

   public void init(String name, Properties _props, MetricRegistry registry, MetricFilter filter) throws Exception {
      if(isInit.compareAndSet(false, true)) {
         init(name, _props);
         ScheduledNewRelicReporter.Builder builder = ScheduledNewRelicReporter.newBuilder(registry);
         final Properties kvProps = new InitUtil(REPORT_PREFIX, _props, false).getProperties();
         for(Object objKey : kvProps.keySet()) {
            String key = objKey.toString();
            EnumSet<MetricField> fields = MetricField.setFromString(kvProps.getProperty(key));
            if(!fields.isEmpty()) {
               builder.addReportedMetric(key, fields);
            }
         }
         builder.setCategory(init.getProperty("category", "custom"));
         builder.convertDurationsTo(TimeUnit.valueOf(init.getProperty(DURATION_UNIT_PROPERTY, "MILLISECONDS").toUpperCase()));
         builder.convertRatesTo(TimeUnit.valueOf(init.getProperty(RATE_UNIT_PROPERTY, "SECONDS").toUpperCase()));
         if(filter != null) {
            builder.filter(filter);
         }
         reporter = builder.build();
         frequencyMillis = InitUtil.millisFromTime(init.getProperty(FREQUENCY_PROPERTY, "1m"));
      }
   }

   @Override
   public void start() throws Exception {
      if(!isInit.get()) {
         throw new InitializationException("The reporter must be initialized before start!");
      }
      if(isRunning.compareAndSet(false, true)) {
         reporter.start(frequencyMillis, TimeUnit.MILLISECONDS);
      }
   }

   @Override
   public void stop() {
      if(isRunning.compareAndSet(true, false)) {
         reporter.stop();
      }
   }

   @Override
   public Map<String, Metric> getMetrics() {
      return reporter.getMetrics();
   }

   private ScheduledNewRelicReporter reporter;
   private long frequencyMillis;
   private final AtomicBoolean isRunning = new AtomicBoolean(false);
}