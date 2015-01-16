package com.ndpmedia.rocketmq.cockpit.controller;

import com.ndpmedia.rocketmq.authentication.RocketMQUserDetailsService;
import com.ndpmedia.rocketmq.authentication.model.CaptchaException;
import com.ndpmedia.rocketmq.authentication.model.RandomValidateCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Produces;

/**
 * the login controller.
 */
@Controller
public class LoginController
{
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public String showIndex()
    {
        System.out.println(" first , we need open the login page .");
        return "login";
    }

    @RequestMapping(value = "/home", method = {RequestMethod.GET, RequestMethod.POST})
    public String login()
    {
        return "home";
    }
}
