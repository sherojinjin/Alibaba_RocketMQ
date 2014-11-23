package com.ndpmedia.rocketmq.cockpit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/broker")
public class BrokerController {

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showIndex() {
        return new ModelAndView("broker/index");

    }


}
