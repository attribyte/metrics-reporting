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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import org.attribyte.util.InitUtil;

import java.util.Properties;

/**
 * Builds a new registry from an existing one
 * by adding just the configured named metrics
 * and translating the name. This is useful
 * for targets like CloudWatch where format
 * and number of metrics are critical.
 * For example: cloudwatch.translate.
 */
public class RegistryTranslation {

   public static final String TRANSLATE_PREFIX = "translate.";

   public static MetricRegistry translate(final Properties props, final MetricRegistry registry) {

      final MetricRegistry translateRegistry = new MetricRegistry();
      final Properties kvProps = new InitUtil(TRANSLATE_PREFIX, props, false).getProperties();

      //When this listener is added, it is notified of all existing metrics.

      registry.addListener(new MetricRegistryListener() {
         @Override
         public void onGaugeAdded(final String s, final Gauge<?> gauge) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.register(translateName, gauge);
            }
         }

         @Override
         public void onGaugeRemoved(final String s) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.remove(translateName);
            }
         }

         @Override
         public void onCounterAdded(final String s, final Counter counter) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.register(translateName, counter);
            }
         }

         @Override
         public void onCounterRemoved(final String s) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.remove(translateName);
            }
         }

         @Override
         public void onHistogramAdded(final String s, final Histogram histogram) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.register(translateName, histogram);
            }
         }

         @Override
         public void onHistogramRemoved(final String s) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.remove(translateName);
            }
         }

         @Override
         public void onMeterAdded(final String s, final Meter meter) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.register(translateName, meter);
            }
         }

         @Override
         public void onMeterRemoved(final String s) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.remove(translateName);
            }
         }

         @Override
         public void onTimerAdded(final String s, final Timer timer) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.register(translateName, timer);
            }
         }

         @Override
         public void onTimerRemoved(final String s) {
            String translateName = kvProps.getProperty(s);
            if(translateName != null) {
               translateRegistry.remove(translateName);
            }
         }
      });

      return translateRegistry;
   }
}
