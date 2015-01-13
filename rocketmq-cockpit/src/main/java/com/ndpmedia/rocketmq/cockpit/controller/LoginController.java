package com.ndpmedia.rocketmq.cockpit.controller;

import com.ndpmedia.rocketmq.authentication.RocketMQUserLoginService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * the login controller.
 */
@Controller
@RequestMapping(value = "/login")
public class LoginController extends UsernamePasswordAuthenticationFilter
{
    private RocketMQUserLoginService rocketMQUserLoginService;

    @RequestMapping(value = "/" ,method = {RequestMethod.GET , RequestMethod.POST})
    public String showIndex() {
        return "login";
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException
    {
        if(!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String username = this.obtainUsername(request);
            String password = this.obtainPassword(request);

            if(username == null) {
                username = "";
            }

            if(password == null) {
                password = "";
            }

            boolean status = rocketMQUserLoginService.findUserStatus(username);

            if (!status)
            {
                password = "";
            }

            username = username.trim();
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
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
