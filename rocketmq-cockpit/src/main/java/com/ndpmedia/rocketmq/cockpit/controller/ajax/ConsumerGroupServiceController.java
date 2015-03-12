package com.ndpmedia.rocketmq.cockpit.controller.ajax;

import com.ndpmedia.rocketmq.cockpit.model.ConsumerGroup;
import com.ndpmedia.rocketmq.cockpit.mybatis.mapper.ConsumerGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/ajax/consumer-group")
public class ConsumerGroupServiceController {

    @Autowired
    private ConsumerGroupMapper consumerGroupMapper;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<ConsumerGroup> list() {
        return consumerGroupMapper.list();
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ConsumerGroup get(@PathVariable("id") long id) {
        return consumerGroupMapper.get(id);
    }

    @RequestMapping(value = "/cluster-name/{clusterName}", method = RequestMethod.GET)
    @ResponseBody
    public List<ConsumerGroup> listByClusterName(@PathVariable("clusterName") String clusterName) {
        return consumerGroupMapper.listByClusterName(clusterName);
    }

    @RequestMapping(value = "/consumer-group-name/{consumerGroupName}", method = RequestMethod.GET)
    @ResponseBody
    public ConsumerGroup getByConsumerGroupName(@PathVariable("consumerGroupName") String consumerGroupName) {
        return consumerGroupMapper.getByGroupName(consumerGroupName);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public void update(@ModelAttribute ConsumerGroup consumerGroup) {
        consumerGroupMapper.update(consumerGroup);
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") long id) {
        consumerGroupMapper.delete(id);
    }

}
