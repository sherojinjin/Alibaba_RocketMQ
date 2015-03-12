package com.ndpmedia.rocketmq.cockpit.scheduler;

import com.alibaba.rocketmq.common.MixAll;
import com.ndpmedia.rocketmq.cockpit.model.ConsumeProgress;
import com.ndpmedia.rocketmq.cockpit.mybatis.mapper.ConsumeProgressMapper;
import com.ndpmedia.rocketmq.cockpit.service.ConsumeProgressService;
import com.ndpmedia.rocketmq.cockpit.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

@Component
public class TaskScheduler {

    @Autowired
    private ConsumeProgressMapper consumeProgressMapper;

    @Autowired
    private ConsumeProgressService consumeProgressService;

    @Autowired
    private TopicService topicService;


    @Scheduled(fixedRate = 5000)
    public void queryAccumulation() {

        try {
            Set<String> topicList = topicService.fetchTopics();

            List<ConsumeProgress> consumeProgressList;
            for (String topic : topicList) {
                if (!topic.contains(MixAll.RETRY_GROUP_TOPIC_PREFIX))
                    continue;
                consumeProgressList = consumeProgressService
                        .queryConsumerProgress(topic.replace(MixAll.RETRY_GROUP_TOPIC_PREFIX, ""), null, null);
                for (ConsumeProgress cp : consumeProgressList) {
                    if (null == cp || null == cp.getTopic() || null == cp.getBrokerName()) {
                        continue;
                    }
                    consumeProgressMapper.insert(cp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
