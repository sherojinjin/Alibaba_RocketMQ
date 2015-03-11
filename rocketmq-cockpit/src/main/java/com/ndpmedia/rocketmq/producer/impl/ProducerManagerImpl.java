package com.ndpmedia.rocketmq.producer.impl;

import com.alibaba.rocketmq.common.MQVersion;
import com.alibaba.rocketmq.common.protocol.body.Connection;
import com.alibaba.rocketmq.common.protocol.body.ProducerConnection;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.ndpmedia.rocketmq.producer.ProducerManager;
import com.ndpmedia.rocketmq.producer.model.Producer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("producerManager")
public class ProducerManagerImpl implements ProducerManager {
    @Override
    public List<Producer> findProducersByGroupName(String groupName, String topic) {
        List<Producer> producers = new ArrayList<Producer>();
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();

            ProducerConnection pc = defaultMQAdminExt.examineProducerConnectionInfo(groupName, topic);

            int i = 1;
            for (Connection conn : pc.getConnectionSet()) {
                Producer producer = new Producer();
                producer.setId(conn.getClientId());
                producer.setAddr(conn.getClientAddr());
                producer.setLanguage(conn.getLanguage());
                producer.setVersion(MQVersion.getVersionDesc(conn.getVersion()));
                producers.add(producer);
                System.out.printf("%04d  %-32s %-22s %-8s %s\n",//
                        i++,//
                        conn.getClientId(),//
                        conn.getClientAddr(),//
                        conn.getLanguage(),//
                        MQVersion.getVersionDesc(conn.getVersion())//
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            defaultMQAdminExt.shutdown();
        }
        return producers;
    }
}
