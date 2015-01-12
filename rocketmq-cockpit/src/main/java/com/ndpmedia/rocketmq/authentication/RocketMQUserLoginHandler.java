package com.ndpmedia.rocketmq.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2015/1/12.
 */
public class RocketMQUserLoginHandler extends SimpleUrlAuthenticationFailureHandler
{
    private RocketMQUserLoginService rocketMQUserLoginService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException
    {
        String username = request.getParameter("j_username");
        rocketMQUserLoginService.userRetryTimeAdd(username);
        super.onAuthenticationFailure(request, response, exception);
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
