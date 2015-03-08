package com.ndpmedia.rocketmq.topic.model;

import com.alibaba.rocketmq.common.TopicConfig;

/**
 * change topic and topic config to each other.
 */
public class TopicTypeUtil {
    /**
     * change topic to topic config
     *
     * @param topic
     * @return
     */
    public static TopicConfig changeTopicToTopicConfig(Topic topic) {
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setWriteQueueNums(topic.getWrite_queue_num());
        topicConfig.setReadQueueNums(topic.getRead_queue_num());
        topicConfig.setTopicName(topic.getTopic());

        return topicConfig;
    }
}
