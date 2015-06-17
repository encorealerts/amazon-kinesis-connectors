package custom.sidekiq;

import samples.KinesisConnectorExecutor;

import com.amazonaws.services.kinesis.connectors.KinesisConnectorRecordProcessorFactory;

/**
 * Executor to emit records to Sidekiq a queues. The name of the queue and activity name can be set in SidekiqSample.properties
 * properties.
 */
public class SidekiqExecutor extends KinesisConnectorExecutor<String, String> {
    private static final String CONFIG_FILE = "Sidekiq.properties";

    /**
     * Creates a new SidekiqExecutor.
     * 
     * @param configFile
     *        The name of the configuration file to look for on the classpath
     */
    public SidekiqExecutor(String configFile) {
        super(configFile);
    }

    @Override
    public KinesisConnectorRecordProcessorFactory<String, String>
            getKinesisConnectorRecordProcessorFactory() {
        return new KinesisConnectorRecordProcessorFactory<String, String>(new SidekiqPipeline(), this.config);
    }

    /**
     * Main method to run the SidekiqExecutor.
     * 
     * @param args
     */
    public static void main(String[] args) {
        KinesisConnectorExecutor<String, String> sidekiqExecutor = new SidekiqExecutor(CONFIG_FILE);
        sidekiqExecutor.run();
    }
}
