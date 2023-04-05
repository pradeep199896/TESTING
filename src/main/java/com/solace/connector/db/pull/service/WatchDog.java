package com.solace.connector.db.pull.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class WatchDog {

    @Resource
    @Lazy
    DataCollecttingService service ;

    @Async("watchDogPool")
    public void checkBufferedEvents() throws  Exception{
        while (true && DataCollecttingService.ready!=null && DataCollecttingService.ready.get()){
            Thread.sleep(1000L);
            if(DataCollecttingService.ready.get()){
                service.checkBufferedEvents();
            }
        }
    }

}
