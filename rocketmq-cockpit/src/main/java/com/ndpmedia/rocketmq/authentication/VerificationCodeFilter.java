package com.ndpmedia.rocketmq.authentication;

import com.ndpmedia.rocketmq.authentication.model.CaptchaException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2015/1/13.
 */
public class VerificationCodeFilter extends UsernamePasswordAuthenticationFilter
{
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException
    {
        String requestCaptcha = request.getParameter("code");
        String genCaptcha = (String)request.getSession().getAttribute("code");

        logger.info("开始校验验证码，生成的验证码为："+genCaptcha+" ，输入的验证码为："+requestCaptcha);

        if( !genCaptcha.equals(requestCaptcha))
        {
            throw new CaptchaException("");
        }

        return super.attemptAuthentication(request, response);
    }
}
