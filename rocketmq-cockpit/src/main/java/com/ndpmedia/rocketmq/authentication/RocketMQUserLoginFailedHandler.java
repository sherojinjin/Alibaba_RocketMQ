package com.ndpmedia.rocketmq.authentication;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * try to process login message when login failed.
 */
public class RocketMQUserLoginFailedHandler extends SimpleUrlAuthenticationFailureHandler
{
    private RocketMQUserLoginService rocketMQUserLoginService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException
    {
        String username = request.getParameter("j_username");
        rocketMQUserLoginService.userRetryTimeAdd(username);
        boolean status = rocketMQUserLoginService.findUserStatus(username);

        if (!status)
        {
            exception.addSuppressed(new Exception(" the user : [" + username + "] is locked !"));
        }

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
