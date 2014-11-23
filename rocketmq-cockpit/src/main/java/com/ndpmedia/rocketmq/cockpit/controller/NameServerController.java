package com.ndpmedia.rocketmq.cockpit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/name-server")
public class NameServerController {

    @RequestMapping(value = "/" ,method = RequestMethod.GET)
    public String showIndex() {
        return "name-server/index";
    }

}
