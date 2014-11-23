package com.ndpmedia.rocketmq.cockpit.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BrokerScheduler {

    @Scheduled(fixedRate = 1000)
    public void queryAccumulation() {
        System.out.println("Broker Accumulation");
    }

}
