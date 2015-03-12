package com.ndpmedia.rocketmq.cockpit.service;

import com.ndpmedia.rocketmq.cockpit.model.ConsumeProgress;

import java.util.List;

public interface ConsumeProgressService {
    List<ConsumeProgress> queryConsumerProgress(String groupName, String topic, String broker);

}
