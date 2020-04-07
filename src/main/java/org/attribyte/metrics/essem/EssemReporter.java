/*
 * Copyright 2015, 2016 Attribyte, LLC
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

package org.attribyte.metrics.essem;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.attribyte.api.InitializationException;
import org.attribyte.essem.reporter.Proto3Builder;
import org.attribyte.essem.reporter.Proto3Reporter;
import org.attribyte.metrics.Reporter;
import org.attribyte.metrics.ReporterBase;
import org.attribyte.util.InitUtil;

import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class EssemReporter extends ReporterBase implements Reporter {

   /**
    * The graphite host ('url').
    */
   public static final String SERVER_URL_PROPERTY = "url";

   /**
    * The graphite host ('username').
    */
   public static final String SERVER_USERNAME_PROPERTY = "username";

   /**
    * The graphite host ('password').
    */
   public static final String SERVER_PASSWORD_PROPERTY = "password";

   /**
    * The host identification sent with reports ('host').
    */
   public static final String REPORT_HOST_PROPERTY = "host";

   /**
    * The application name sent with reports ('application').
    */
   public static final String REPORT_APPLICATION_PROPERTY = "application";

   /**
    * The application instance sent with reports ('instance').
    */
   public static final String REPORT_INSTANCE_PROPERTY = "instance";

   /**
    * Should deflate be used when sending reports ('deflate')? Default 'true'.
    */
   public static final String REPORT_DEFLATE_PROPERTY = "deflate";

   /**
    * Should unchanged metrics (since last report) be skipped ('skipUnchanged'). Default 'false'.
    */
   public static final String SKIP_UNCHANGED__PROPERTY = "skipUnchanged";

   /**
    * Identifies the HDR histogram/timer report mode ('none', 'total', 'snapshot').
    * <p>
    *    Default is 'snapshot'.
    * </p>
    */
   public static final String HDR_REPORT_PROPERTY = "hdrReport";

   @Override
   public void init(final String name,
                    final Properties _props, final MetricRegistry registry,
                    final MetricFilter filter) throws Exception {
      if(isInit.compareAndSet(false, true)) {
         init(name, _props);
         if(init.getProperty(SERVER_URL_PROPERTY, "").isEmpty()) {
            init.throwRequiredException(SERVER_URL_PROPERTY);
         }

         URI uri = new URI(init.getProperty(SERVER_URL_PROPERTY));
         Proto3Builder builder =
                 Proto3Reporter.newBuilder(uri, registry);

         if(filter != null) {
            builder.filter(filter);
         }

         String username = init.getProperty(SERVER_USERNAME_PROPERTY, "");
         String password = init.getProperty(SERVER_PASSWORD_PROPERTY, "");
         if(!username.isEmpty()) {
            builder.withBasicAuthorization(username, password);
         }

         builder.withDeflate(init.getProperty(REPORT_DEFLATE_PROPERTY, "true").equalsIgnoreCase("true"));

         builder.forApplication(init.getProperty(REPORT_APPLICATION_PROPERTY))
                 .forHost(init.getProperty(REPORT_HOST_PROPERTY))
                 .forInstance(init.getProperty(REPORT_INSTANCE_PROPERTY));

         builder.convertDurationsTo(TimeUnit.valueOf(init.getProperty(DURATION_UNIT_PROPERTY, "MILLISECONDS").toUpperCase()));
         builder.convertRatesTo(TimeUnit.valueOf(init.getProperty(RATE_UNIT_PROPERTY, "SECONDS").toUpperCase()));
         builder.skipUnchangedMetrics(init.getProperty(SKIP_UNCHANGED__PROPERTY, "false").equalsIgnoreCase("true"));

         String hdrMode = init.getProperty(HDR_REPORT_PROPERTY, "snapshot");
         switch(hdrMode.toLowerCase()) {
            case "snapshot":
               builder.setHdrReport(org.attribyte.essem.reporter.EssemReporter.HdrReport.SNAPSHOT);
               break;
            case "total":
               builder.setHdrReport(org.attribyte.essem.reporter.EssemReporter.HdrReport.TOTAL);
               break;
            case "none":
               builder.setHdrReport(org.attribyte.essem.reporter.EssemReporter.HdrReport.NONE);
               break;
            default:
               builder.setHdrReport(org.attribyte.essem.reporter.EssemReporter.HdrReport.SNAPSHOT);
               break;
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
   public final Map<String, Metric> getMetrics() {
      return reporter.getMetrics();
   }

   private org.attribyte.essem.reporter.EssemReporter reporter;
   private long frequencyMillis;
   private final AtomicBoolean isRunning = new AtomicBoolean(false);
}
