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

import org.attribyte.util.InitUtil;

import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ReporterBase implements Reporter {

   /**
    * The reporting frequency as a string ('frequency').
    * For example: 30s, 1m, 5m, 1h. Default is one minute and probably
    * should not be changed.
    */
   public static String FREQUENCY_PROPERTY = "frequency";

   /**
    * The report duration unit ('durationUnit').
    * For example: MILLISECONDS
    */
   public static String DURATION_UNIT_PROPERTY = "durationUnit";

   /**
    * The report rate unit ('rateUnit').
    * For example: SECONDS, MINUTES
    */
   public static String RATE_UNIT_PROPERTY = "rateUnit";

   @Override
   public String getName() {
      return name;
   }

   /**
    * Initialize the properties.
    * @param name The instance name.
    * @param props The properties.
    */
   protected void init(final String name, final Properties props) {
      init = new InitUtil("", props, false);
      this.name = name;
   }

   /**
    * Gets the hostname.
    * @return The hostname.
    */
   protected String getHostname() {
      try {
         return java.net.InetAddress.getLocalHost().getHostName();
      } catch(UnknownHostException ue) {
         return "[unknown]";
      }
   }

   protected InitUtil init;
   protected String name;
   protected final AtomicBoolean isInit = new AtomicBoolean(false);
}