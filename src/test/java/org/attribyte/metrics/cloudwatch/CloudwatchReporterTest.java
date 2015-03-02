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

package org.attribyte.metrics.cloudwatch;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.attribyte.metrics.RegistryTranslation;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Properties;

public class CloudwatchReporterTest {

   @Test
   public void reportToCloudwatch() throws Exception  {

      Properties secrets = new Properties();
      String userHome = System.getProperty("user.home");
      try(FileInputStream fis = new FileInputStream(userHome+"/cwsecrets")) {
         secrets.load(fis);
      }

      Properties props = new Properties(secrets);
      props.put(CloudwatchReporter.METRIC_NAMESPACE_PROPERTY, "test01");
      props.put(RegistryTranslation.TRANSLATE_PREFIX + "internal-counter-02", "counter-02");

      MetricRegistry registry = new MetricRegistry();
      Counter counter01 = registry.counter("internal-counter-01");
      counter01.inc(14L);
      Counter counter02 = registry.counter("internal-counter-02");
      counter02.inc(15L);
      Counter counter03 = registry.counter("internal-counter-03");
      counter03.inc(16L);

      CloudwatchReporter reporter = new CloudwatchReporter();
      reporter.init("cloudwatch", props, registry, null); //No filter...
      reporter.start();
      Thread.sleep(61000L);
      reporter.stop();
   }
}
