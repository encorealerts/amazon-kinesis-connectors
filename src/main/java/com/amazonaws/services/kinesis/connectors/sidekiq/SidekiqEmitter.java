/*
 * Copyright 2013-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.kinesis.connectors.sidekiq;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;
import com.amazonaws.services.kinesis.connectors.UnmodifiableBuffer;
import com.amazonaws.services.kinesis.connectors.interfaces.IEmitter;
import com.amazonaws.services.s3.AmazonS3Client;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Random;

import redis.clients.jedis.Jedis;

/**
 * This implementation of IEmitter is used to store files from an Amazon Kinesis stream in S3. The use of
 * this class requires the configuration of an Amazon S3 bucket/endpoint. When the buffer is full, this
 * class's emit method adds the contents of the buffer to Amazon S3 as one file. The filename is generated
 * from the first and last sequence numbers of the records contained in that file separated by a
 * dash. This class requires the configuration of an Amazon S3 bucket and endpoint.
 */
public class SidekiqEmitter implements IEmitter<String> {
    private static final Log LOG = LogFactory.getLog(SidekiqEmitter.class);
    protected final String sidekiqNamespace;
    protected final String sidekiqQueueName;
    protected final String sidekiqTaskName;
    protected final String sidekiqTaskRetry;
    protected final String sidekiqRedisEndpoint;

    public SidekiqEmitter(KinesisConnectorConfiguration configuration) {
        sidekiqNamespace = configuration.SIDEKIQ_NAMESPACE;
        sidekiqQueueName = configuration.SIDEKIQ_QUEUE_NAME;
        sidekiqTaskName = configuration.SIDEKIQ_TASK_NAME;
        sidekiqTaskRetry = configuration.SIDEKIQ_TASK_RETRY;
        sidekiqRedisEndpoint = configuration.SIDEKIQ_REDIS_ENDPOINT;
    }

    protected Jedis getRedisClient(String s3FileName) {
        return new Jedis(sidekiqRedisEndpoint);
    }

    @Override
    public List<String> emit(final UnmodifiableBuffer<String> buffer) throws IOException {
        List<String> records = buffer.getRecords();
        
        try {
            LOG.debug("Enqueuing task for sidekiq into " + sidekiqRedisEndpoint + " containing " + records.size() + " records.");
            for (String record : records) {
                System.out.println(record);

                JSONArray args = new JSONArray();
                args.put(record);

                JSONObject message = new JSONObject();
                message.put("class", sidekiqTaskName);
                message.put("args", (Object)args);
                message.put("jid", getRandomHexString(12));
                message.put("retry", sidekiqTaskRetry);
                message.put("enqueued_at", Calendar.getInstance().getTime().getMinutes()/60.0f);

                Jedis jedis = new Jedis(sidekiqRedisEndpoint);
                jedis.lpush(sidekiqNamespace + ":queue:" + sidekiqQueueName, message.toString());
            }
            LOG.info("Successfully enqueued " + buffer.getRecords().size() + " records to Sidekiq in " + sidekiqRedisEndpoint);
            return Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Caught exception when enqueuing " + sidekiqRedisEndpoint + " to Sidekiq. Failing this emit attempt.", e);
            return buffer.getRecords();
        }
    }

    private String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

    @Override
    public void fail(List<String> records) {
        for (String record : records) {
            LOG.error("Record failed: " + record);
        }
    }

    @Override
    public void shutdown() {
        LOG.info("Shutting down SidekiqEmitter");            
    }

}
