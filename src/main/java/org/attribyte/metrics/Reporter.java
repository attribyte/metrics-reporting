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

import java.util.Properties;

public interface Reporter {

   /**
    * Initialize a reporter instance.
    * @param name An instance name.
    * @param props The properties.
    * @param registry The registry to report.
    * @param filter An optional filter.
    * @throws Exception on initialization error.
    */
   public void init(String name, Properties props, MetricRegistry registry, MetricFilter filter) throws Exception;

   /**
    * Starts the reporter.
    * @throws Exception on start error.
    */
   public void start() throws Exception;

   /**
    * Stops the reporter.
    */
   public void stop();

   /**
    * Gets the reporter name.
    * @return The name.
    */
   public String getName();

}
