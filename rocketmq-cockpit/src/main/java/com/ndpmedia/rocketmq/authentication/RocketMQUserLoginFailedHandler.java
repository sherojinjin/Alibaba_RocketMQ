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
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException
    {
        String username = request.getParameter("j_username");
        int retryTime = 0;
        try
        {
            retryTime = Integer.parseInt("" + request.getSession().getAttribute(username));
        }
        catch (Exception e)
        {

        }
        System.out.println("login failed , this user [" + username + "] already retry " + retryTime);
        request.getSession().setAttribute(username, retryTime + 1);
        if (retryTime >= 5)
        {
            exception.addSuppressed(new Exception(" the user : [" + username + "] is locked !"));
        }
        this.setDefaultFailureUrl("/cockpit/login");
        super.onAuthenticationFailure(request, response, exception);
    }
}
