package com.ndpmedia.rocketmq.cockpit.controller.service;

import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.ndpmedia.rocketmq.cockpit.service.NameServerKVService;
import com.ndpmedia.rocketmq.cockpit.model.KV;
import com.ndpmedia.rocketmq.cockpit.model.KVStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/ajax/name-server/kv")
public class NameServerKVServiceController {

    @Autowired
    @Qualifier("nameServerKVService")
    private NameServerKVService nameServerKVService;

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public KV add(@ModelAttribute KV kv) {
        long id = nameServerKVService.add(kv);
        kv.setId(id);
        return kv;
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public KV view(@PathVariable("id") long id) {
        KV kv = nameServerKVService.get(id);
        return kv;
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.POST)
    @ResponseBody
    public KV apply(@PathVariable("id") long id) throws Exception {
        KV kv = nameServerKVService.get(id);
        if (null != kv) {
            DefaultMQAdminExt mqAdmin = new DefaultMQAdminExt();
            mqAdmin.createAndUpdateKvConfig(kv.getNameSpace(), kv.getKey(), kv.getValue());
            kv.setStatus(KVStatus.ACTIVE);
            nameServerKVService.update(kv);
        }
        return kv;
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public KV delete(@PathVariable("id") long id) {
        KV kv = nameServerKVService.get(id);
        if (null != kv) {
            nameServerKVService.delete(kv);
            kv.setStatus(KVStatus.DELETED);
        }
        return kv;
    }

    @RequestMapping
    @ResponseBody
    public List<KV> list() {
        return nameServerKVService.list();
    }

    @RequestMapping(value = "/{status}", method = RequestMethod.GET)
    @ResponseBody
    public List<KV> list(@PathVariable(value = "status") String status) {
        return nameServerKVService.list(KVStatus.valueOf(status));
    }

}
