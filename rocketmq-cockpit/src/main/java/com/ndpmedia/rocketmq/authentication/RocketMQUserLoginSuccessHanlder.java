package com.ndpmedia.rocketmq.authentication;

import com.ndpmedia.rocketmq.cockpit.util.LoginConstant;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

/**
 * try to save user login session.
 */
public class RocketMQUserLoginSuccessHanlder extends SavedRequestAwareAuthenticationSuccessHandler
        implements LoginConstant
{
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        Cookie[] cookies = request.getCookies();

        for (Cookie c : cookies)
        {
            System.out.println(c.getName() + " [request.getRemoteHost()] " + c.getValue());
        }

        Cookie users = getCookie(request, LOGIN_PARAMETER_USERNAME, request.getParameter(LOGIN_PARAMETER_USERNAME));
        Cookie pass = getCookie(request, LOGIN_PARAMETER_PASSWORD, request.getParameter(LOGIN_PARAMETER_PASSWORD));

        Collection<? extends GrantedAuthority> c = authentication.getAuthorities();
        StringBuffer grant = new StringBuffer();
        for (GrantedAuthority g : c)
        {
            System.out.println(g.getAuthority());
            grant.append(g.getAuthority());
            grant.append(";");
        }

        Cookie auth = getCookie(request, LOGIN_PARAMETER_AUTHORITY, grant.toString());
        response.addCookie(users);
        response.addCookie(pass);
        response.addCookie(auth);

        super.onAuthenticationSuccess(request, response, authentication);
    }

    public static String getIpAddr(HttpServletRequest request)
    {
        String ip = getServerIP(request);
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("x-forwarded-for");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }
        //当使用本地域名访问时，获取到的IP系特殊IP 0:0:0:0:0:0:0:1
        if (ip.startsWith("0:0:0:0:"))
        {
            ip = "localhost";
        }

        return ip;
    }

    private static String getServerIP(HttpServletRequest request)
    {
        StringBuffer url = request.getRequestURL();
        System.out.println(url);
        return url.toString();
    }

    public static Cookie getCookie(HttpServletRequest request, String key, String value)
    {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setDomain(getIpAddr(request));
        cookie.setHttpOnly(true);
        System.out.println(cookie.toString());
        return cookie;
    }
}
