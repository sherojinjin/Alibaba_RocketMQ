package com.ndpmedia.rocketmq.cockpit.controller.api;

import com.ndpmedia.rocketmq.cockpit.model.Topic;
import com.ndpmedia.rocketmq.cockpit.mybatis.mapper.TopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public Topic add(@ModelAttribute Topic topic) {
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
    public void update(@ModelAttribute Topic topic) {
        topicMapper.update(topic);
    }
}
