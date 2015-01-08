<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Cockpit Home</title>
    <%@include file="include/base-path.jsp"%>
    <base href="<%=basePath%>">
</head>
<body>
    <h1>Cockpit Home</h1>

    <ul>
        <li><a href="cockpit/name-server/">Manage Name Server</a></li>
        <li><a href="cockpit/ip/">Manage IP Mapping</a></li>
        <li><a href="cockpit/broker/">Broker Status</a></li>
    </ul>

</body>
</html>
