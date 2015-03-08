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
    <link href="http://libs.baidu.com/bootstrap/3.0.3/css/bootstrap.min.css" rel="stylesheet">
    <script src="http://libs.baidu.com/bootstrap/3.0.3/js/bootstrap.min.js" type="application/javascript"></script>
    <script src="js/name-server-kv.js" type="application/javascript"></script>
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
                    </tbody>
                </table>
                <div class="clear-both"></div>
            </div>
        </div>

        <div class="clear-both"></div>

        <div class="row">
            <div class="col-xs-8 col-xs-offset-2 text-left table-responsive">
                <div class="col-xs-3">
                    <input type="text" class="form-control nameSpace" placeholder="Name Space">
                </div>

                <div class="col-xs-3">
                    <input type="text" class="form-control key" placeholder="Key">
                </div>

                <div class="col-xs-4">
                    <input type="text" class="form-control value" placeholder="Value">
                </div>

                <div class="col-xs-2">
                    <button type="submit" class="btn btn-primary addKV">Add</button>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
