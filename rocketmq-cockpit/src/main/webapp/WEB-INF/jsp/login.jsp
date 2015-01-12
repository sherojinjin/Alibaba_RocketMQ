
<body>
${Session["SPRING_SECURITY_LAST_EXCEPTION"]?default('')}
<span style="color:red"><%=session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) %></span>
<form id="loginForm"
    action="./j_spring_security_check" method="post">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
     <tr>
      <th>用户名：</th>
      <td><input type="text" name="j_username" id="username"
       class="input"
       value="<c:if test='${not empty param.login_error}' >
               <c:out value='${SPRING_SECURITY_LAST_USERNAME}'/>
               </c:if>" />
      </td>
     </tr>
     <tr>
      <th>密&nbsp;&nbsp;&nbsp;&nbsp; 码：</th>
      <td><input type="password" name="j_password" id="password"
       class="input" /></td>
     </tr>
     <tr>
      <td rowspan="3"><a onclick="javascript:login();">登录</a></td>
     </tr>
    </table>
   </form>
   <c:if test="${not empty param.error}">
    <font color="red"> 登录失败<br /> <br /> 原因: <c:out
      value="${SPRING_SECURITY_LAST_EXCEPTION.message}" /></font>
   </c:if>

</body>
