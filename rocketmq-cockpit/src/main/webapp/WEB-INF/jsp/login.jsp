<%@ page language="java" contentType="text/html;charset=UTF-8" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Login Page</title>
<%@include file="include/base-path.jsp"%>
    <base href="<%=basePath%>">
<script type="text/javascript">
    function refresh(obj) {

    }
    </script>
</head>
<body onload='document.f.j_username.focus();'>
<h3>Login with Username and Password</h3>
<form name='f' action='/j_spring_security_check' method='POST'>
 <table>
    <tr><td colspan='2'><span style="color:red"><%=session.getAttribute("errorMSG") %></span> </td></tr>
    <tr><td>User:</td><td><input type='text' name='j_username' value=''></td></tr>
    <tr><td>Password:</td><td><input type='password' name='j_password'/></td></tr>
    <tr><td colspan='2'><input name="submit" type="submit" value="Login"/></td></tr>
    <tr><td>verification code</td><td>
    <input type="text" name="randomCode"/>
    </td>
</tr>
  </table>
</form></body></html>