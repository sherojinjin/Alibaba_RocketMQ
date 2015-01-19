<%@ page language="java" contentType="text/html;charset=UTF-8" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Login Page</title>
<script src="http://libs.baidu.com/jquery/1.7.0/jquery.js"></script>
    <script src="http://libs.baidu.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
    <link href="http://libs.baidu.com/bootstrap/3.0.3/css/bootstrap.min.css" rel="stylesheet">
<%@include file="include/base-path.jsp"%>
    <base href="<%=basePath%>">
<script type="text/javascript">
    function check() {
        var username = document.getElementById("j_username").value;
        var password = document.getElementById("j_password").value;
        var kaptcha = document.getElementById("kaptcha").value;
        if(!username)
        {
            alert(" user name can not be null !");
            return false;
        }
        if(!password)
        {
            alert(" password can not be null !");
            return false;
        }
        if(!kaptcha)
        {
            alert(" kaptcha can not be null !");
            return false;
        }
        return true;
    }
    </script>
</head>
<body onload='document.f.j_username.focus();'>
<h3>Login with Username and Password</h3>
<form name='f' action='/j_spring_security_check' method='POST'>
<%
String msg = "";
Object errMSG = session.getAttribute("errorMSG");
Object errSMSG =  session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
if (null != errMSG){
    msg = (String)errMSG;
   } else if (null != errSMSG){
    msg = errSMSG.toString();
    }
%>
 <table width="100%">
    <tr><td colspan='2' width="100%" ><span style="color:red"><%=msg%></span></td></tr>
    <tr><td width="20%">User:</td><td><input type='text' id="j_username" name='j_username' value=''></td></tr>
    <tr><td width="20%">Password:</td><td><input type='password' id="j_password" name='j_password'/></td></tr>
<tr><td width="20%">
  <div class="chknumber">
         <label>verification code：</label></td>
         <td><input name="kaptcha" type="text" id="kaptcha" maxlength="8" class="chknumber_input" />
         <img src="/cockpit/captcha-image" width="140" height="40" id="kaptchaImage"  style="margin-bottom: -3px"/>
         <script type="text/javascript">
          $(function(){
              $('#kaptchaImage').click(function () {//生成验证码
               $(this).hide().attr('src', '/cockpit/captcha-image?' + Math.floor(Math.random()*100) ).fadeIn(); })

                    });

         </script>
  </div>
  </td></tr>
    <tr><td colspan='2' width="100%" ><input name="submit" type="submit" value="Login" onclick="return check()
    "/></td></tr>
  </table>
</form></body></html>