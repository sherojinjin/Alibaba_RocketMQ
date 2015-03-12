package com.ndpmedia.rocketmq.cockpit.controller.ajax;

import com.ndpmedia.rocketmq.cockpit.model.Topic;
import com.ndpmedia.rocketmq.cockpit.mybatis.mapper.TopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/ajax/topic")
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
        long id = topicMapper.insert(topic);
        topic.setId(id);
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
