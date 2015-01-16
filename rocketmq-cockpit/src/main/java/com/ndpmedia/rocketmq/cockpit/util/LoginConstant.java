package com.ndpmedia.rocketmq.cockpit.util;

/**
 * Created by Administrator on 2015/1/14.
 */
public interface LoginConstant extends Constant
{
    String LOGIN_TOO_MANY_TIMES_MSG = " you try too many times !";

    String LOGIN_VERIFICATION_CODE_WRONG = " please check your verification code !";

    String LOGIN_SESSION_ERROR_KEY = "errorMSG";

    String LOGIN_PARAMETER_USERNAME = "j_username";

    String LOGIN_PARAMETER_PASSWORD = "j_password";

    String LOGIN_PARAMETER_KAPTCHA = "kaptcha";

    String LOGIN_PAGE_PATH = "/cockpit/login.jsp";

    String PROPERTIES_KEY_LOGIN_RETRY_TIME = "login_retry_time" ;

    String PROPERTIES_KEY_JDBC_DRIVER = "jdbc.driverClassName";

    String PROPERTIES_KEY_JDBC_URL = "";
}
