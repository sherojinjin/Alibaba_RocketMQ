package com.ndpmedia.rocketmq.cockpit.controller.api;

import com.ndpmedia.rocketmq.cockpit.model.Topic;
import com.ndpmedia.rocketmq.cockpit.mybatis.mapper.TopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/api/topic")
public class TopicServiceController {

    @Autowired
    private TopicMapper topicMapper;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Topic> list() {
        return topicMapper.list();
    }

    @RequestMapping(value = "/{topic}", method = RequestMethod.GET)
    @ResponseBody
    public List<Topic> lookUp(@PathVariable("topic") String topic) {
        return topicMapper.listByTopic(topic);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Topic add(@RequestBody Topic topic) {
        topicMapper.insert(topic);
        return topic;
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("id") long id) {
        topicMapper.delete(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public void update(@RequestBody Topic topic) {
        topic.setUpdateTime(new Date());
        topicMapper.update(topic);
    }
}
