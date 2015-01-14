package com.ndpmedia.rocketmq.cockpit.controller;

import com.ndpmedia.rocketmq.authentication.RocketMQUserDetailsService;
import com.ndpmedia.rocketmq.authentication.model.CaptchaException;
import com.ndpmedia.rocketmq.authentication.model.RandomValidateCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Produces;

/**
 * the login controller.
 */
@Controller
public class LoginController
{
    @Autowired
    private RocketMQUserDetailsService userService;

    @Autowired
    private AuthenticationManager myAuthenticationManager;

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public String showIndex()
    {
        System.out.println(" first , we need open the login page .");
        return "login";
    }

    @RequestMapping(value = "/doLogin", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(@RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password, HttpServletRequest request)
    {
//        if (!checkValidateCode(request))
//        {
//            return "login";
//        }
        username = username.trim();
        System.out.println(" try to check " + username);
        int retryTime = null == request.getSession().getAttribute(username) ? 0 : (1 + Integer.parseInt((String)
                request.getSession().getAttribute(username)));
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
/*		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(CwSysUser.class,"cwSysUser");
    detachedCriteria.add(Restrictions.eq("userNo", username));
    if(cwSysUserService.countUser(detachedCriteria)==0){
      return new LoginInfo().failed().msg("用户名: "+username+" 不存在.");
    }
*/
        try
        {
            if (retryTime >= 5)
                throw  new CaptchaException(" retry too many times ");
            Authentication authentication = myAuthenticationManager.authenticate(authRequest); //调用loadUserByUsername
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = request.getSession();
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext()); // 这个非常重要，否则验证后将无法登陆
            session.setAttribute(username, null);
            return "home";
        }
        catch (AuthenticationException ex)
        {
            ex.printStackTrace();
            request.getSession().setAttribute(username, retryTime);
            return "login";
        }
    }

    @RequestMapping(value = "/home", method = {RequestMethod.GET, RequestMethod.POST})
    public String login()
    {

        return "home";
    }


    @RequestMapping(value = "/verification")
    public void verification(HttpServletRequest request, HttpServletResponse response)
    {
        System.out.println("try to get verification code");
        response.setContentType("image/jpeg");//设置相应类型,告诉浏览器输出的内容为图片
        response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);
        RandomValidateCode randomValidateCode = new RandomValidateCode();
        try {
            randomValidateCode.getRandcode(request, response);//输出图片方法
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 验证码判断
     *
     * @param request
     * @return
     */
    protected boolean checkValidateCode(HttpServletRequest request)
    {
        String result_verifyCode = request.getSession().getAttribute("verifyResult").toString(); // 获取存于session的验证值
        // request.getSession().setAttribute("verifyResult", null);
        String user_verifyCode = request.getParameter("verifyCode");// 获取用户输入验证码
        if (null == user_verifyCode || !result_verifyCode.equalsIgnoreCase(user_verifyCode))
        {
            return false;
        }
        return true;
    }

}
