package com.ndpmedia.rocketmq.cockpit.controller;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.ndpmedia.rocketmq.nameserver.NameServerKVService;
import com.ndpmedia.rocketmq.nameserver.model.KV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/name-server")
public class NameServerController {

    @Autowired
    @Qualifier("nameServerKVService")
    private NameServerKVService nameServerKVService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndex() {
        return "name-server/index";
    }


    @RequestMapping(value = "/kv", method = RequestMethod.GET)
    public ModelAndView listKV() {
        ModelAndView modelAndView = new ModelAndView("name-server/kv/list");
        modelAndView.addObject("list", nameServerKVService.list());
        return modelAndView;
    }


    @RequestMapping(value = "/kv", method = RequestMethod.PUT)
    public String add(@ModelAttribute KV kv) {
        nameServerKVService.add(kv);
        return "forward:/cockpit/name-server/kv";
    }

    @RequestMapping(value = "/kv", method = RequestMethod.POST)
    public String update(@ModelAttribute KV kv) {
        nameServerKVService.update(kv);
        return "forward:/cockpit/name-server/kv";
    }

    @RequestMapping(value = "/kv/{id}", method = RequestMethod.GET)
    public String apply(@PathVariable("id")long id)
            throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        KV kv = nameServerKVService.get(id);
        DefaultMQAdminExt mqAdmin = new DefaultMQAdminExt();
        mqAdmin.createAndUpdateKvConfig(kv.getNameSpace(), kv.getKey(), kv.getValue());
        return "forward:/cockpit/name-server/kv";
    }
}
