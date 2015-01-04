<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Cockpit Home</title>
    <%@include file="include/base-path.jsp"%>
    <base href="<%=basePath%>">
    <link rel="shortcut icon" href="favicon.ico">
</head>
<body>
    <h1>Cockpit Home</h1>

    <ul>
        <li><a href="cockpit/name-server/">Manage Name Server</a></li>
        <li><a href="cockpit/ip/">Manage IP Mapping</a></li>
        <li><a href="cockpit/broker/">Broker Status</a></li>
    </ul>

    <h1>Console Home</h1>
    <ul>
        <li><a href="console/">Manage</a></li>
    </ul>
</body>
</html>
