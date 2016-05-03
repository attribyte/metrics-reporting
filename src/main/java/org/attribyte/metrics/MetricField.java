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

package org.attribyte.metrics;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum MetricField {

   //Counting...

   /**
    * The count.
    */
   COUNT,

   //Metered...

   /**
    * The one minute rate.
    */
   ONE_MINUTE_RATE,

   /**
    * The five minute rate.
    */
   FIVE_MINUTE_RATE,

   /**
    * The fifteen minute rate.
    */
   FIFTEEN_MINUTE_RATE,

   /**
    * The mean rate.
    */
   MEAN_RATE,


   //Sampling...

   /**
    * The median value.
    */
   MEDIAN,

   /**
    * The 75th percentile value.
    */
   P75,

   /**
    * The 95th percentile value.
    */
   P95,

   /**
    * The 98th percentile value.
    */
   P98,

   /**
    * The 99th percentile value.
    */
   P99,

   /**
    * The 99.9th percentile value.
    */
   P999,

   /**
    * The maximum value.
    */
   MAX,

   /**
    * The mean value.
    */
   MEAN,

   /**
    * The minimum value.
    */
   MIN,

   /**
    * The standard deviation.
    */
   STD,

   //Gauge, counter

   /**
    * The value for a gauge or counter.
    */
   VALUE,

   /**
    * An unknown field.
    */
   UNKNOWN;

   /**
    * Creates set of fields from a comma-separated string.
    * @param str The string.
    * @return The set of fields.
    */
   public static final EnumSet<MetricField> setFromString(final String str) {
      return EnumSet.copyOf(
              Splitter.on(',')
                      .omitEmptyStrings()
                      .trimResults()
                      .splitToList(str)
                      .stream()
                      .map(MetricField::fromString)
                      .collect(Collectors.toSet())
      );
   }

   /**
    * Gets a metric field from a string value.
    * @param str The string value.
    * @return The metric field or <code>UNKNOWN</code>.
    */
   public static final MetricField fromString(final String str) {
      switch(Strings.nullToEmpty(str).trim().toLowerCase().replace('-', '_').replace(' ', '_')) {
         case "count":
            return COUNT;
         case "one_minute_rate":
         case "1m_rate":
            return ONE_MINUTE_RATE;
         case "five_minute_rate":
         case "5m_rate":
            return FIVE_MINUTE_RATE;
         case "fifteen_minute_rate":
         case "15m_rate":
            return FIFTEEN_MINUTE_RATE;
         case "mean_rate":
            return MEAN_RATE;
         case "median":
         case "p50":
         case "50th":
         case "50th_%":
         case "50th_percentile":
            return MEDIAN;
         case "p75":
         case "75th":
         case "75th_%":
         case "75th_percentile":
            return P75;
         case "p95":
         case "95th":
         case "95th_%":
         case "95th_percentile":
            return P95;
         case "p98":
         case "98th":
         case "98th_%":
         case "98th_percentile":
            return P98;
         case "p99":
         case "99th":
         case "99th_%":
         case "99th_percentile":
            return P99;
         case "p999":
         case "99.9th":
         case "99.9th_%":
         case "99.9th_percentile":
            return P999;
         case "max":
            return MAX;
         case "mean":
         case "avg":
            return MEAN;
         case "min":
            return MIN;
         case "std":
         case "standard_deviation":
            return STD;
         case "value":
            return VALUE;
         default:
            return UNKNOWN;
      }
   }
}