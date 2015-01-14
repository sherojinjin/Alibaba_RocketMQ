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
 <table>
    <tr><td colspan='2'><span style="color:red"><%=msg%></span></td></tr>
    <tr><td>User:</td><td><input type='text' name='j_username' value=''></td></tr>
    <tr><td>Password:</td><td><input type='password' name='j_password'/></td></tr>
    <tr><td colspan='2'><input name="submit" type="submit" value="Login"/></td></tr>
    
  </table>
</form></body></html>