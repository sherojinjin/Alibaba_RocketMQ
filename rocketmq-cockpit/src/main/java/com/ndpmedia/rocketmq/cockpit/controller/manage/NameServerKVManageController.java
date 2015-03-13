package com.ndpmedia.rocketmq.cockpit.controller.manage;

import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.ndpmedia.rocketmq.cockpit.model.KV;
import com.ndpmedia.rocketmq.cockpit.model.Status;
import com.ndpmedia.rocketmq.cockpit.service.NameServerKVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/manage/name-server-kv")
public class NameServerKVManageController {

    @Autowired
    private NameServerKVService nameServerKVService;

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public KV apply(@PathVariable("id") long id) throws Exception {
        KV kv = nameServerKVService.get(id);
        if (null != kv) {
            DefaultMQAdminExt mqAdmin = new DefaultMQAdminExt();
            mqAdmin.createAndUpdateKvConfig(kv.getNameSpace(), kv.getKey(), kv.getValue());
            kv.setStatus(Status.ACTIVE);
            nameServerKVService.update(kv);
        }
        return kv;
    }

}
