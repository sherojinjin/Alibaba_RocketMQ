package com.alibaba.rocketmq.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2015/1/20.
 */
@Controller
@RequestMapping("/authority")
public class AutoLoginAction
{
    @Autowired
    private AuthenticationManager myAuthenticationManager;

    @RequestMapping(value = "/login.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response)
    {
        ServletContext servletContext = request.getServletContext();

        HttpSession session = null;
        String uid = null;
        String password = null;

        Collection<? extends GrantedAuthority> authorities = null;

        try
        {
            Cookie[] cookies = request.getCookies();
            for (Cookie c : cookies)
            {
                System.out.println(c.getName() + " [request.getRemoteHost()] " + c.getValue());
                if (c.getName().contains("username"))
                {
                    uid = c.getValue();
                }
                if (c.getName().contains("password"))
                {
                    password = c.getValue();
                }
                if (c.getName().contains("authority"))
                {
                    authorities = getAuthority(c.getValue());
                }
            }

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(uid, password,
                    authorities);

            // generate session if one doesn't exist
            request.getSession();

            token.setDetails(new WebAuthenticationDetails(request));
            Authentication authenticatedUser = myAuthenticationManager.authenticate(token);

            SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

            request.getRequestDispatcher("../cluster/list.do").forward(request, response);

        }
        catch (Exception e)
        {

        }
        return null;
    }


    public Collection<GrantedAuthority> getAuthority(String role)
    {
        List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
        if (role.contains(";"))
        {
            String[] roles = role.split(";");
            for (String ro : roles)
                authList.add(new SimpleGrantedAuthority(ro));
        }
        else
        {
            authList.add(new SimpleGrantedAuthority(role));
        }
        return authList;
    }

    public AuthenticationManager getMyAuthenticationManager()
    {
        return myAuthenticationManager;
    }

    public void setMyAuthenticationManager(AuthenticationManager myAuthenticationManager)
    {
        this.myAuthenticationManager = myAuthenticationManager;
    }
}
