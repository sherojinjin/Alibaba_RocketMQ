<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>IP Mapping Management</title>
  <%@include file="../include/base-path.jsp"%>
  <base href="<%=basePath%>%">
  <link rel="shortcut icon" href="favicon.ico" />
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <script src="http://libs.baidu.com/jquery/1.7.0/jquery.js"></script>
  <script src="http://libs.baidu.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
  <link href="http://libs.baidu.com/bootstrap/3.0.3/css/bootstrap.min.css" rel="stylesheet">
  <script src="js/ip-mapping.js" type="application/javascript"></script>
</head>
<body>
<div class="container-fluid">
  <div class="row">
    <div class="col-md-8 col-md-offset-2 text-center">
      <h1>IP Mapping Catalog</h1>
    </div>
  </div>

  <div class="clear-both"></div>

  <div class="row">
    <div class="col-xs-8 col-xs-offset-2 text-left table-responsive">
      <table class="table table-condense table-hover table-bordered">
        <thead>
        <tr>
          <td>Inner IP Address</td>
          <td>Public IP Address</td>
          <td>Operation</td>
        </tr>
        </thead>
        <tbody class="table-striped table-content">
        </tbody>
      </table>

      <div class="clear-both"></div>

    </div>
    <div class="col-xs-4 col-xs-offset-2">
      <input type="text" class="form-control innerIP" placeholder="Inner IP Address">
    </div>

    <div class="col-xs-4">
      <input type="text" class="form-control publicIP" placeholder="Public IP Address">
    </div>

    <div class="col-xs-2">
      <button type="submit" class="btn btn-primary addMapping">Add</button>
    </div>
  </div>
</div>
</body>
</html>
