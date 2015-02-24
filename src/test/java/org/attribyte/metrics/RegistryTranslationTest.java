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

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Properties;

import static org.junit.Assert.*;

public class RegistryTranslationTest {

   @Test
   public void translateNames()  {

      Properties props = new Properties();
      props.put(RegistryTranslation.TRANSLATE_PREFIX + "meter-02", "translated-meter-02");

      MetricRegistry registry = new MetricRegistry();
      registry.meter("meter-01");
      registry.meter("meter-02");
      registry.meter("meter-03");

      MetricRegistry translated = RegistryTranslation.translate(props, registry);
      assertEquals(1, translated.getMeters().size());
      assertNotNull(translated.getMeters().get("translated-meter-02"));
   }

   @Test
   public void addAfterTranslated()  {

      Properties props = new Properties();
      props.put(RegistryTranslation.TRANSLATE_PREFIX + "meter-02", "translated-meter-02");

      MetricRegistry registry = new MetricRegistry();
      registry.meter("meter-01");
      registry.meter("meter-03");

      MetricRegistry translated = RegistryTranslation.translate(props, registry);
      assertEquals(0, translated.getMeters().size());

      registry.meter("meter-02");
      assertEquals(1, translated.getMeters().size());
      assertNotNull(translated.getMeters().get("translated-meter-02"));
   }

   @Test
   public void removeAfterTranslated()  {

      Properties props = new Properties();
      props.put(RegistryTranslation.TRANSLATE_PREFIX + "meter-02", "translated-meter-02");

      MetricRegistry registry = new MetricRegistry();
      registry.meter("meter-01");
      registry.meter("meter-02");
      registry.meter("meter-03");

      MetricRegistry translated = RegistryTranslation.translate(props, registry);
      assertEquals(1, translated.getMeters().size());
      assertNotNull(translated.getMeters().get("translated-meter-02"));

      registry.remove("meter-02");
      assertEquals(0, translated.getMeters().size());
      assertNull(translated.getMeters().get("translated-meter-02"));
   }

}
