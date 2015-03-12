package com.ndpmedia.rocketmq.cockpit.controller.ajax;

import com.ndpmedia.rocketmq.cockpit.model.NameServer;
import com.ndpmedia.rocketmq.cockpit.mybatis.mapper.NameServerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping(value = "/ajax/name-server")
public class NameServerServiceController {

    @Autowired
    private NameServerMapper nameServerMapper;

    @RequestMapping(method = RequestMethod.GET)
    public List<NameServer> list() {
        return nameServerMapper.list();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public NameServer get(@PathVariable("id") long id) {
        return nameServerMapper.get(id);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public NameServer add(@ModelAttribute NameServer nameServer) {
        long id = nameServerMapper.insert(nameServer);
        nameServer.setId(id);
        return nameServer;
    }

    @RequestMapping(method = RequestMethod.POST)
    public NameServer update(@ModelAttribute NameServer nameServer) {
        long id = nameServerMapper.insert(nameServer);
        nameServer.setId(id);
        return nameServer;
    }
}
