package com.ndpmedia.rocketmq.authentication;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * personal filter.just for check retry times.
 */
public class RocketMQLoginFilter implements Filter
{
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
        String username = request.getParameter("j_username");
        session.removeAttribute("errorMSG");
        if (null != session && null != session.getAttribute(username))
        {
            int retryTime = Integer.parseInt("" + session.getAttribute(username));
            if (retryTime >= 5)
            {
                System.out.println(" retry too many times !" + retryTime);
                session.setAttribute("errorMSG"," you try too many times !");
                request.getRequestDispatcher("/cockpit/login.jsp").forward(request,response);
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
