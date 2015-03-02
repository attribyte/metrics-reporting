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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
import com.blacklocus.metrics.CloudWatchReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.attribyte.api.InitializationException;
import org.attribyte.metrics.RegistryTranslation;
import org.attribyte.metrics.Reporter;
import org.attribyte.metrics.ReporterBase;
import org.attribyte.util.InitUtil;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reports metrics to CloudWatch.
 * <p>
 *    You'll want to be very careful about the names and format of metrics
 *    you report to CloudWatch. It can be expensive. As such, this reporter
 *    expects to extract and rename particular metrics from the full registry
 *    that, for a normal application, is likely to contain far more metrics than you'll want to report.
 *    They are defined as properties like this:
 *      <code>metrics-reporting.cloudwatch.translate.org.attribyte.pubsub.callbacks=NumberOfMessagesSent</code>
 *    See: https://github.com/blacklocus/metrics-cloudwatch
 *      "In case you forgot, AWS costs money. Metrics and monitoring can easily become the most expensive part of your stack.
 *      So be wary of metrics explosions."
 *    If you really want to use the full registry set: <code>metrics-reporting.cloudwatch.disableTranslate=true</code>
 * </p>
 */
public class CloudwatchReporter extends ReporterBase implements Reporter {

   /**
    * The AWS access key id ('accessKeyId').
    */
   public static final String AWS_ACCESS_KEY_ID_PROPERTY =  "accessKeyId";

   /**
    * The AWS access key secret ('accessKeySecret').
    */
   public static final String AWS_ACCESS_KEY_SECRET_PROPERTY = "accessKeySecret";

   /**
    * The namespace to which metrics will be reported ('metricNamespace').
    */
   public static final String METRIC_NAMESPACE_PROPERTY = "metricNamespace";

   /**
    * Disable translation and use the metrics as they are in the original registry.
    */
   public static final String DISABLE_TRANSLATE_PROPERTY = "disableTranslate";

   /**
    * Should an instance profile be used?
    */
   public static final String USE_INSTANCE_CREDENTIALS_PROPERTY = "useInstanceCredentials";

   /**
    * Should the default provider chain be used?
    */
   public static final String USE_DEFAULT_PROVIDER_CHAIN_PROPERTY = "useDefaultProviderChain";

   @Override
   public void init(final String name,
                    final Properties _props, final MetricRegistry registry,
                    final MetricFilter filter) throws Exception {
      if(isInit.compareAndSet(false, true)) {
         init(name, _props);

         boolean useInstanceCredentials = init.getProperty(USE_INSTANCE_CREDENTIALS_PROPERTY, "false").equalsIgnoreCase("true");
         boolean useDefaultProviderChain = init.getProperty(USE_DEFAULT_PROVIDER_CHAIN_PROPERTY, "false").equalsIgnoreCase("true");

         if(useDefaultProviderChain) {
            client = new AmazonCloudWatchAsyncClient(new DefaultAWSCredentialsProviderChain());
         } else if(useInstanceCredentials) {
            client = new AmazonCloudWatchAsyncClient(new InstanceProfileCredentialsProvider());
         } else {
            String awsKeyId = init.getProperty(AWS_ACCESS_KEY_ID_PROPERTY, "");
            if(awsKeyId.isEmpty()) {
               init.throwRequiredException(AWS_ACCESS_KEY_ID_PROPERTY);
            }

            String awsKeySecret = init.getProperty(AWS_ACCESS_KEY_SECRET_PROPERTY, "");
            if(awsKeySecret.isEmpty()) {
               init.throwRequiredException(AWS_ACCESS_KEY_SECRET_PROPERTY);
            }

            client = new AmazonCloudWatchAsyncClient(new BasicAWSCredentials(awsKeyId, awsKeySecret));
         }

         String cloudwatchNamespace = init.getProperty(METRIC_NAMESPACE_PROPERTY, null);

         frequencyMillis = InitUtil.millisFromTime(init.getProperty(FREQUENCY_PROPERTY, "1m"));

         boolean disableTranslate = init.getProperty(DISABLE_TRANSLATE_PROPERTY, "false").equalsIgnoreCase("true");

         if(disableTranslate) {
            reporter = new CloudWatchReporter(registry, cloudwatchNamespace, client);
         } else {
            MetricRegistry filteredRegistry = RegistryTranslation.translate(init.getProperties(), registry);
            reporter = new CloudWatchReporter(filteredRegistry, cloudwatchNamespace, client);
         }
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
         client.shutdown();
      }
   }

   private CloudWatchReporter reporter;
   private long frequencyMillis;

   private AmazonCloudWatchAsyncClient client;
   private final AtomicBoolean isRunning = new AtomicBoolean(false);
}