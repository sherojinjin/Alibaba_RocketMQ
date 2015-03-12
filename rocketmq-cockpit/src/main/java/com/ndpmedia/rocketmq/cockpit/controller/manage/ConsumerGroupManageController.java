package com.ndpmedia.rocketmq.cockpit.controller.manage;

import com.ndpmedia.rocketmq.cockpit.model.ConsumerGroup;
import com.ndpmedia.rocketmq.cockpit.service.ConsumerGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/manage/consumer-group")
public class ConsumerGroupManageController {

    @Autowired
    private ConsumerGroupService consumerGroupService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public boolean update(@ModelAttribute ConsumerGroup consumerGroup) {
        return consumerGroupService.update(consumerGroup);
    }

}
