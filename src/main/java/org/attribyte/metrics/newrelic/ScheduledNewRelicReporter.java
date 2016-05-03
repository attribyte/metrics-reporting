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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.newrelic.api.agent.NewRelic;
import org.attribyte.metrics.MetricField;

import java.util.EnumSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A scheduled metric reporter that reports to New Relic.
 */
public class ScheduledNewRelicReporter extends ScheduledReporter implements MetricSet {

   /**
    * Creates a builder.
    * @param registry the registry to report
    * @return The builder.
    */
   public static Builder newBuilder(final MetricRegistry registry) {
      return new Builder(registry);
   }

   public static class Builder {

      /**
       * Creates a builder.
       * @param registry The registry to report.
       */
      private Builder(final MetricRegistry registry) {
         this.registry = registry;
         this.filter = MetricFilter.ALL;
      }

      /**
       * Configures the rate conversion. Default is seconds.
       * @param rateUnit The rate unit.
       * @return A self-reference.
       */
      public Builder convertRatesTo(final TimeUnit rateUnit) {
         this.rateUnit = rateUnit;
         return this;
      }

      /**
       * Configures the duration conversion. Default is milliseconds.
       * @param durationUnit The duration unit.
       * @return A self-reference.
       */
      public Builder convertDurationsTo(final TimeUnit durationUnit) {
         this.durationUnit = durationUnit;
         return this;
      }

      /**
       * Applies a filter to the registry before reporting.
       * @param filter The filter.
       * @return A self-reference.
       */
      public Builder filter(final MetricFilter filter) {
         this.filter = filter;
         return this;
      }

      /**
       * Adds a reported metric.
       * @param name The name.
       * @param fields The fields.
       * @return A self-reference.
       */
      public Builder addReportedMetric(final String name, final EnumSet<MetricField> fields) {
         this.reportedMetrics.put(name, fields);
         return this;
      }

      /**
       * Sets the New Relic category.
       * @param category The category.
       * @return A self-reference.
       */
      public Builder setCategory(final String category) {
         this.category = category;
         return this;
      }

      /**
       * Builds an immutable reporter instance.
       * @return The immutable reporter.
       */
      public ScheduledNewRelicReporter build() {
         return new ScheduledNewRelicReporter(registry, filter, rateUnit, durationUnit,
                 reportedMetrics, category);
      }

      private final MetricRegistry registry;

      private TimeUnit rateUnit = TimeUnit.SECONDS;
      private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
      private MetricFilter filter;
      private final Map<String, EnumSet<MetricField>> reportedMetrics = Maps.newHashMap();
      private String category = "custom";
   }

   protected ScheduledNewRelicReporter(final MetricRegistry registry,
                                       final MetricFilter filter,
                                       final TimeUnit rateUnit,
                                       final TimeUnit durationUnit,
                                       final Map<String, EnumSet<MetricField>> reportedMetrics,
                                       final String category) {
      super(registry, "newrelic-reporter", filter, rateUnit, durationUnit);
      this.reportedMetrics = reportedMetrics != null ? ImmutableMap.copyOf(reportedMetrics) : ImmutableMap.of();
      this.category = category;
      this.rateUnit = "[per " + toLabel(rateUnit) + "]";
      this.durationUnit = "[" + toLabel(durationUnit) + "]";
   }

   /**
    * Converts a time unit to a label.
    * @param unit The time unit.
    * @return The label.
    */
   private String toLabel(final TimeUnit unit) {
      switch(unit) {
         case DAYS: return "day";
         case HOURS: return "hour";
         case MINUTES: return "minute";
         case SECONDS: return "second";
         case MILLISECONDS: return "ms";
         case MICROSECONDS: return "us";
         case NANOSECONDS: return "ns";
         default: return "";
      }
   }

   /**
    * Reports a meter/timer.
    * @param name The name.
    * @param meter The meter.
    */
   private void reportMetered(final String name, final Metered meter) {
      EnumSet<MetricField> fields = reportedMetrics.get(name);
      if(fields == null) {
         return;
      }

      String nrName = buildRate(name);

      if(fields.contains(MetricField.ONE_MINUTE_RATE)) {
         NewRelic.recordMetric(nrName, (float)meter.getOneMinuteRate());
      }

      if(fields.contains(MetricField.FIVE_MINUTE_RATE)) {
         NewRelic.recordMetric(nrName, (float)meter.getFiveMinuteRate());
      }

      if(fields.contains(MetricField.FIFTEEN_MINUTE_RATE)) {
         NewRelic.recordMetric(nrName, (float)meter.getOneMinuteRate());
      }

      if(fields.contains(MetricField.MEAN_RATE)) {
         NewRelic.recordMetric(nrName, (float)meter.getMeanRate());
      }

      if(fields.contains(MetricField.COUNT)) {
         NewRelic.recordMetric(nrName, (float)meter.getCount());
      }
   }

   /**
    * Reports a sampling (timer, histogram) metric.
    * @param name The name.
    * @param sampled The sampled metric.
    * @param isDuration Is this a duration.
    */
   private void reportSampled(final String name,
                              final Sampling sampled,
                              final boolean isDuration) {
      EnumSet<MetricField> fields = reportedMetrics.get(name);
      if(fields == null) {
         return;
      }

      String nrName = isDuration ? buildDuration(name) : buildName(name);

      if(fields.contains(MetricField.MEDIAN)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().getMedian());
      }

      if(fields.contains(MetricField.MAX)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().getMax());
      }

      if(fields.contains(MetricField.MIN)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().getMin());
      }

      if(fields.contains(MetricField.P75)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().get75thPercentile());
      }

      if(fields.contains(MetricField.P95)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().get95thPercentile());
      }

      if(fields.contains(MetricField.P98)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().get98thPercentile());
      }

      if(fields.contains(MetricField.P99)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().get99thPercentile());
      }

      if(fields.contains(MetricField.P999)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().get999thPercentile());
      }

      if(fields.contains(MetricField.STD)) {
         NewRelic.recordMetric(nrName, (float)sampled.getSnapshot().getStdDev());
      }
   }


   @Override
   public void report(SortedMap<String, Gauge> gauges,
                      SortedMap<String, Counter> counters,
                      SortedMap<String, Histogram> histograms,
                      SortedMap<String, Meter> meters,
                      SortedMap<String, Timer> timers) {

      lastMetricCount.set(gauges.size() + counters.size() + histograms.size() + meters.size() + timers.size());

      for(Map.Entry<String, Gauge> gauge : gauges.entrySet()) {
         String name = gauge.getKey();
         EnumSet<MetricField> fields = reportedMetrics.get(name);
         if(fields != null && fields.contains(MetricField.VALUE)) {
            Object val = gauge.getValue().getValue();
            if(val instanceof Number) {
               NewRelic.recordMetric(buildName(name), ((Number)val).floatValue());
            }
         }
      }

      for(Map.Entry<String, Counter> counter : counters.entrySet()) {
         String name = counter.getKey();
         EnumSet<MetricField> fields = reportedMetrics.get(name);
         if(fields != null && fields.contains(MetricField.VALUE)) {
            long value = counter.getValue().getCount();
            NewRelic.recordMetric(buildName(name), (float)value);
         }
      }

      for(Map.Entry<String, Meter> nv : meters.entrySet()) {
         String name = nv.getKey();
         Meter meter = nv.getValue();
         reportMetered(name, meter);
      }

      for(Map.Entry<String, Histogram> nv : histograms.entrySet()) {
         String name = nv.getKey();
         Histogram histogram = nv.getValue();
         reportSampled(name, histogram, false);
      }

      for(Map.Entry<String, Timer> nv : timers.entrySet()) {
         final String name = nv.getKey();
         Timer timer = nv.getValue();
         reportMetered(name, timer);
         reportSampled(name, timer, true);
      }
   }

   /**
    * The set of names/fields to report.
    */
   private final ImmutableMap<String, EnumSet<MetricField>> reportedMetrics;

   /**
    * The reported rate unit.
    */
   private final String rateUnit;

   /**
    * The reported duration unit.
    */
   private final String durationUnit;

   /**
    * The reported category.
    */
   private final String category;

   @Override
   public Map<String, Metric> getMetrics() {
      return metrics;
   }

   private final ImmutableMap<String, Metric> metrics =
           ImmutableMap.of(
                   "report-count", new Gauge<Integer>() {
                      public Integer getValue() {
                         return lastMetricCount.get();
                      }
                   }
           );

   /**
    * Builds the New Relic name.
    * @param name The name.
    * @return The New Relic name.
    */
   private String buildName(final String name) {
      return "Custom/" + category + "/" + name;
   }

   /**
    * Builds the New Relic name for a rate.
    * @param name The name.
    * @return The New Relic name.
    */
   private String buildRate(final String name) {
      return "Custom/" + category + "/" + name + rateUnit;
   }

   /**
    * Builds the New Relic name for a duration.
    * @param name The name.
    * @return The New Relic Name.
    */
   private String buildDuration(final String name) {
      return "Custom/" + category + "/" + name + durationUnit;
   }

   /**
    * The number of metrics last reported.
    */
   private final AtomicInteger lastMetricCount = new AtomicInteger();
}