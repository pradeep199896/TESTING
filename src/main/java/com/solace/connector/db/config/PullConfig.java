package com.solace.connector.db.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pull")
@Data
public class PullConfig {
    int sendBatchSize = 1 ;
    int queryMax =1000 ;

    long triggerInterval = 1000;
    int retryInterval = 5 ;
    int retryCount = 3 ;
    boolean enableRetry =true ;

    boolean enableBufferedJpaBatch =true ;
    int bufferWaitSeconds = 5;

    boolean enableLVQ = false;
    String LVQTopicName = "lvq" ;
    boolean enableTrackingId= true;
    boolean enableDatabaseTimestamp = true;
    boolean enableConnectorTimestamp = true;

    boolean enableBenchmarkInfo =false;
}
