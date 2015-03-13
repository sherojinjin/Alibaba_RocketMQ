package com.ndpmedia.rocketmq.cockpit.controller.api;

import com.ndpmedia.rocketmq.cockpit.model.KV;
import com.ndpmedia.rocketmq.cockpit.model.Status;
import com.ndpmedia.rocketmq.cockpit.service.NameServerKVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/api/name-server-kv")
public class NameServerKVServiceController {

    @Autowired
    @Qualifier("nameServerKVService")
    private NameServerKVService nameServerKVService;

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public KV add(@RequestBody KV kv) {
        nameServerKVService.add(kv);
        return kv;
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public KV view(@PathVariable("id") long id) {
        return nameServerKVService.get(id);
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public KV delete(@PathVariable("id") long id) {
        KV kv = nameServerKVService.get(id);
        if (null != kv) {
            nameServerKVService.delete(kv);
            kv.setStatus(Status.DELETED);
        }
        return kv;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<KV> list() {
        return nameServerKVService.list();
    }

    @RequestMapping(value = "/status/{status}", method = RequestMethod.GET)
    @ResponseBody
    public List<KV> list(@PathVariable(value = "status") String status) {
        return nameServerKVService.list(Status.valueOf(status));
    }
}
