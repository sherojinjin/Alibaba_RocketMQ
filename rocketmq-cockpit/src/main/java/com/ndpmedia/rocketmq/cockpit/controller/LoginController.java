package com.ndpmedia.rocketmq.cockpit.controller;

import com.ndpmedia.rocketmq.authentication.RocketMQUserLoginService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * the login controller.
 */
@Controller
@RequestMapping(value = "/login")
public class LoginController
{
    private RocketMQUserLoginService rocketMQUserLoginService;

    @RequestMapping(value = "/" ,method = {RequestMethod.GET , RequestMethod.POST})
    public String showIndex() {
        return "login";
    }

    public RocketMQUserLoginService getRocketMQUserLoginService()
    {
        return rocketMQUserLoginService;
    }

    public void setRocketMQUserLoginService(RocketMQUserLoginService rocketMQUserLoginService)
    {
        this.rocketMQUserLoginService = rocketMQUserLoginService;
    }
}
