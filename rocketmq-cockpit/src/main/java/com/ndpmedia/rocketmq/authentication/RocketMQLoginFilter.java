package com.ndpmedia.rocketmq.authentication;

import com.google.code.kaptcha.Constants;
import com.ndpmedia.rocketmq.cockpit.util.LoginConstant;
import com.ndpmedia.rocketmq.io.FileManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
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

        session.removeAttribute(LOGIN_SESSION_ERROR_KEY);

        if (checkRetryTimes(request))
        {
            System.out.println(" retry too many times !" );
            session.setAttribute(LOGIN_SESSION_ERROR_KEY, LOGIN_TOO_MANY_TIMES_MSG);
            request.getRequestDispatcher(LOGIN_PAGE_PATH).forward(request, response);
            return;
        }

        if (checkVerificationCode(request))
        {
            System.out.println(" verification code is not right !");
            session.setAttribute(LOGIN_SESSION_ERROR_KEY, LOGIN_VERIFICATION_CODE_WRONG);
            request.getRequestDispatcher(LOGIN_PAGE_PATH).forward(request, response);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy()
    {

    }

    /**
     *  check how many times try to login.
     * @param request
     * @return  if login too many times,return true;
     */
    private boolean checkRetryTimes(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String username = request.getParameter(LOGIN_PARAMETER_USERNAME);
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
                return true;
            }
        }
        return false;
    }

    /**
     *  check the input verification code
     * @param request
     * @return  verify ok return false;
     */
    private boolean checkVerificationCode(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        try
        {
            Enumeration<String> s = request.getParameterNames();
            while (s.hasMoreElements())
            {
                String temp = s.nextElement();
                System.out.println("[name]:" + temp + "[values]:" + request.getParameter(temp));
            }
            String kaptcha = request.getParameter(LOGIN_PARAMETER_KAPTCHA);
            if (null == kaptcha || kaptcha.isEmpty())
                return false;
            System.out.println(LOGIN_PARAMETER_KAPTCHA + " [=] " + kaptcha);
            String code = (String)session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
            System.out.println(Constants.KAPTCHA_SESSION_KEY + " [=] " + code);
            if (kaptcha.equals(code))
                return false;
        }
        catch (Exception e)
        {
            System.out.println(" try to check verification code failed !");
        }
        return true;
    }
}
