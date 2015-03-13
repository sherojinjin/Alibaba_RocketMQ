<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Topic Management</title>
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
      <h1>Topic Catalog</h1>
    </div>
  </div>

  <div class="clear-both"></div>

  <div class="row">
    <div class="col-xs-8 col-xs-offset-2 text-left table-responsive">
      <table class="table table-hover table-bordered">
        <thead>
        <tr>
          <td style="display:none">id</td>
          <td style="display:none">cluster_name</td>
          <td style="display:none">permission</td>
          <td style="display:none">write_queue_num</td>
          <td style="display:none">read_queue_num</td>
          <td style="display:none">unit</td>
          <td style="display:none">has_unit_subscription</td>
          <td style="display:none">broker_address</td>
          <td style="display:none">order_type</td>
          <td style="display:none">status_id</td>
          <td style="display:none">create_time</td>
          <td style="display:none">update_time</td>
          <td>topic</td>
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
        <tr><td>writeQueueNum:</td><td>  <input type="text" class="form-control writeQueueNum"
        placeholder="writeQueueNum"></td></tr>
        <tr><td>readQueueNum:</td><td>  <input type="text" class="form-control readQueueNum"
        placeholder="readQueueNum"></td></tr>
        <tr><td>brokerAddress:</td><td>  <input type="text" class="form-control brokerAddress"
        placeholder="brokerAddress"></td></tr>
        <tr><td>clusterName:</td><td>  <input type="text" class="form-control clusterName"
        placeholder="clusterName"></td></tr>
        <tr><td>clusterName:</td><td>  <input type="text" class="form-control permission"
                placeholder="permission"></td></tr>
        <tr><td>clusterName:</td><td>  <input type="text" class="form-control unit"
                placeholder="unit"></td></tr>
        <tr><td>clusterName:</td><td>  <input type="text" class="form-control hasUnitSubscription"
                placeholder="hasUnitSubscription"></td></tr>
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
