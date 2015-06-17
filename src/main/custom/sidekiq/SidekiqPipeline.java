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
package custom.sidekiq;

import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;
import com.amazonaws.services.kinesis.connectors.impl.AllPassFilter;
import com.amazonaws.services.kinesis.connectors.impl.BasicMemoryBuffer;
import com.amazonaws.services.kinesis.connectors.impl.StringToStringTransformer;
import com.amazonaws.services.kinesis.connectors.interfaces.IBuffer;
import com.amazonaws.services.kinesis.connectors.interfaces.IEmitter;
import com.amazonaws.services.kinesis.connectors.interfaces.IFilter;
import com.amazonaws.services.kinesis.connectors.interfaces.IKinesisConnectorPipeline;
import com.amazonaws.services.kinesis.connectors.interfaces.ITransformer;
import com.amazonaws.services.kinesis.connectors.sidekiq.SidekiqEmitter;

/**
 * The Pipeline used by the Amazon S3 sample. Processes String records in JSON String
 * format. Uses:
 * <ul>
 * <li>SidekiqEmitter</li>
 * <li>BasicMemoryBuffer</li>
 * <li>StringToStringTransformer</li>
 * <li>AllPassFilter</li>
 * </ul>
 */
public class SidekiqPipeline implements IKinesisConnectorPipeline<String, String> {

    @Override
    public IEmitter<String> getEmitter(KinesisConnectorConfiguration configuration) {
        return new SidekiqEmitter(configuration);
    }

    @Override
    public IBuffer<String> getBuffer(KinesisConnectorConfiguration configuration) {
        return new BasicMemoryBuffer<String>(configuration);
    }

    @Override
    public ITransformer<String, String> getTransformer(KinesisConnectorConfiguration configuration) {
        return new StringToStringTransformer();
    }

    @Override
    public IFilter<String> getFilter(KinesisConnectorConfiguration configuration) {
        return new AllPassFilter<String>();
    }

}
