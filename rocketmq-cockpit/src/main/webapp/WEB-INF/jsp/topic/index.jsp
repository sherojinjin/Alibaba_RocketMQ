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
  <script src="js/topic.js" type="application/javascript"></script>
</head>
<body>
<div class="container-fluid">
  <div class="row">
    <div class="col-md-8 col-md-offset-2 text-center">
      <h1>topic Catalog</h1>
    </div>
  </div>

  <div class="clear-both"></div>

  <div class="row">
    <div class="col-xs-8 col-xs-offset-2 text-left table-responsive">
      <table class="table table-hover table-bordered">
        <thead>
        <tr>
          <td>topic</td>
          <td></td>
          <td>Operation</td>
        </tr>
        </thead>
        <tbody class="table-striped table-content">
        </tbody>
      </table>

      <div class="clear-both"></div>

    </div>
  </div>
</div>

  <div class="clear-both"></div>

<div class="col-xs-8 col-xs-offset-2 text-left table-responsive">
    <table class="table table-bordered">
        <tr><td>topic:</td><td>  <input type="text" class="form-control topic" placeholder="topic"></td></tr>
        <tr><td>write_queue_num:</td><td>  <input type="text" class="form-control write_queue_num"
        placeholder="write_queue_num"></td></tr>
        <tr><td>read_queue_num:</td><td>  <input type="text" class="form-control read_queue_num"
        placeholder="read_queue_num"></td></tr>
        <tr><td>broker_address:</td><td>  <input type="text" class="form-control broker_address"
        placeholder="broker_address"></td></tr>
        <tr><td>cluster_name:</td><td>  <input type="text" class="form-control cluster_name"
        placeholder="cluster_name"></td></tr>
        <tr><td>order:</td><td>  <input type="text" class="form-control order" placeholder="order"></td></tr>
        <tr><td colspan="2">
    <div class="col-xs-2">
      <button type="submit" class="btn btn-primary addTopic">Add</button>
    </div>
        </td></tr>
    </table>
</div>

</body>
</html>
