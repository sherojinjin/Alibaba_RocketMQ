<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Name Server Management</title>
    <%@include file="../../include/base-path.jsp"%>
    <base href="<%=basePath%>%">
    <link rel="shortcut icon" href="favicon.ico" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <script src="http://libs.baidu.com/jquery/1.7.0/jquery.js"></script>
    <script src="http://libs.baidu.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
    <link href="http://libs.baidu.com/bootstrap/3.0.3/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-8 col-md-offset-2 text-center">
                <h1>Name Server Key-Value List</h1>
            </div>
        </div>

        <div class="clear-both"></div>

        <div class="row">
            <div class="col-xs-8 col-xs-offset-2 text-left table-responsive">
                <table class="table table-condense table-hover table-bordered">
                    <thead>
                        <tr>
                            <td>Name Space</td>
                            <td>Key</td>
                            <td>Value</td>
                            <td>Status</td>
                            <td>Operation</td>
                        </tr>
                    </thead>
                    <tbody class="table-striped table-content">
                        <c:forEach items="${list}" var="item">
                            <tr>
                                <td>${item.nameSpace}</td>
                                <td>${item.key}</td>
                                <td>${item.value}</td>
                                <td>${item.status}</td>
                                <td>
                                    <a href="name-server/kv/${item.id}">Apply</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <div class="clear-both"></div>
            </div>
        </div>
    </div>
</body>
</html>
