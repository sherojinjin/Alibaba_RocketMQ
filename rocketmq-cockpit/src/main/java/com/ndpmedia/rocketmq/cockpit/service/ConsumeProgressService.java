package com.ndpmedia.rocketmq.cockpit.service;

import com.ndpmedia.rocketmq.cockpit.model.ConsumerProgress;

import java.util.List;

public interface ConsumeProgressService {
    List<ConsumerProgress> queryConsumerProgress(String groupName, String topic, String broker);

}
