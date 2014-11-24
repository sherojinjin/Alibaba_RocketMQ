package com.ndpmedia.rocketmq.cockpit.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BrokerScheduler {

    @Scheduled(fixedRate = 5000)
    public void queryAccumulation() {
        System.out.println("Broker Accumulation");
    }

}
