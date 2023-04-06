package com.solace.connector.db.pull.service;

import com.solace.connector.db.config.DbTableIndicatorConfig;
import com.solace.connector.db.config.PullConfig;
import com.solace.connector.db.pull.client.ClientPushService;
import com.solace.connector.db.pull.lock.SynLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TriggerService {

    @Resource
    PullConfig config;

    @Resource
    ClientPushService pushService;

    @Autowired
    StreamBridge streamBridge;

    @Resource
    @Qualifier("triggerPool")
    ThreadPoolTaskExecutor triggerExecutor;

    @Async("triggerPool")
    public void startPolling() throws Exception {
        MessageHeaderAccessor mha = new MessageHeaderAccessor();

        while (DataCollecttingService.ready == null || !DataCollecttingService.ready.get()) {
            if(!DataCollecttingService.globalShutdown.get()) {
                Thread.sleep(1000l);
                log.info("waiting loading config...");
            }
        }

//        while (DataCollecttingService.ready!=null && DataCollecttingService.ready.get()){
        for (DbTableIndicatorConfig tableConfig : DataCollecttingService.tableConfigMap.values()) {
            log.info("scaned config: {}",tableConfig.getTableKey());
            synchronized (DataCollecttingService.contextMap) {
                if (!DataCollecttingService.contextMap.containsKey(tableConfig.getTableKey())) {

                    SynLock synLock = new SynLock();
                    synLock.setTableKey(tableConfig.getTableKey());
                    synLock.setSynStatus(SynLock.SynStatus.Trigger);
                    mha.setHeader(BinderHeaders.TARGET_DESTINATION,tableConfig.getLvqTopic());
//                    mha.setHeader("contentType","plain/text");
                    Message<SynLock> message = MessageBuilder
                            .withPayload(synLock)
                            .setHeaders(mha)
                            .build();
//                    streamBridge.send("output-0",message);
                    pushService.sendJsonMessage(tableConfig.getLvqTopic(), synLock, mha);
                    log.info("{} triggered", tableConfig.getTopic());
                }
            }
        }
//        }
    }
}
