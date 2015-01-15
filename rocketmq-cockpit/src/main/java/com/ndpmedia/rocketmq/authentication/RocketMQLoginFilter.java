package com.ndpmedia.rocketmq.authentication;

import com.ndpmedia.rocketmq.cockpit.util.LoginConstant;
import com.ndpmedia.rocketmq.io.FileManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Properties;

/**
 * personal filter.just for check retry times.
 */
public class RocketMQLoginFilter implements Filter, LoginConstant
{
    private static Properties config;

    static
    {
        config = FileManager.getConfig();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        System.out.println(" personal filter, check retry times ");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String username = request.getParameter(LOGIN_PARAMETER_USERNAME);
        session.removeAttribute(LOGIN_SESSION_ERROR_KEY);
        if (null != session && null != session.getAttribute(username))
        {
            int retryTime = Integer.parseInt("" + session.getAttribute(username));
            int retryTimeMAX = FIVE;
            try
            {
                retryTimeMAX = Integer.parseInt(config.getProperty(PROPERTIES_KEY_LOGIN_RETRY_TIME));
            }
            catch (NumberFormatException e)
            {
                System.out.println(" please check your properties.");
            }
            if (retryTime >= retryTimeMAX)
            {
                System.out.println(" retry too many times !" + retryTime);
                session.setAttribute(LOGIN_SESSION_ERROR_KEY, LOGIN_TOO_MANY_TIMES_MSG);
                request.getRequestDispatcher("/cockpit/login.jsp").forward(request, response);
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy()
    {

    }
}
