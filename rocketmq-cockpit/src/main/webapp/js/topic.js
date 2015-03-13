$(document).ready(function() {

    $.get("cockpit/api/topic", function(data) {
        $(".table-content").children().remove();
        data.forEach(function(topic) {
            var operationLink = $("<a class='removeItem' href='javascript:;'>Remove</a>");
            var approveLink = $("<a class='approveItem' href='javascript:;'>Approve</a>");

            var operation = $("<td></td>");
            if (topic.status != "ACTIVE"){
                operation.append(approveLink);
                operation.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            operation.append(operationLink);
            var item = $("<tr><td style='display:none'>" + topic.id + "</td><td style='display:none'>" + topic.clusterName + "</td><td style='display:none'>" + topic.permission + "</td><td style='display:none'>" + topic.writeQueueNum + "</td><td style='display:none'>" + topic.readQueueNum + "</td><td style='display:none'>" + topic.unit + "</td><td style='display:none'>" + topic.hasUnitSubscription + "</td><td style='display:none'>" + topic.brokerAddress + "</td><td style='display:none'>" + topic.order + "</td><td style='display:none'>" + topic.status + "</td><td style='display:none'>" + topic.createTime + "</td><td style='display:none'>" + topic.updateTime + "</td><td>" + topic.topic + "</td></tr>");
            item.append(operation);
            $(".table-content").append(item);
        });
    });

    $(".addTopic").click(function() {
        var topic = $("input.topic").val();
        var write_queue_num = $("input.writeQueueNum").val();
        var read_queue_num = $("input.readQueueNum").val();
        var broker_address = $("input.brokerAddress").val();
        var cluster_name = $("input.clusterName").val();
        var permission = $("input.permission").val();
        var unit = $("input.unit").val();
        var has_unit_subscription = $("input.hasUnitSubscription").val();
        var order = $("input.order").val();
        var allow = "DRAFT";
        var ob = JSON.stringify({"topic":topic,"writeQueueNum":write_queue_num,"readQueueNum":read_queue_num,
                                 "brokerAddress":broker_address, "clusterName":cluster_name, "permission":permission, "unit":unit, "hasUnitSubscription":has_unit_subscription, "order":order, "status":allow});
        if ($.trim(topic) === "") {
            return false;
        } else if ($.trim(cluster_name) === "" && $.trim(broker_address) == "") {
            return false;
        } else {
            $.ajax({
                        async: false,
                        url: "cockpit/api/topic",
                        type: "PUT",
                        dataType: "json",
                        contentType: 'application/json',
                        data: ob,
                        success: function() {
                            location.reload(true);
                        },
                        error: function() {

                        }
                    });
        }
    });

    $(".removeItem").live("click", function() {
        var row = $(this).parent().parent();
        var id = row.children().eq(0).html();
        var cluster_name = row.children().eq(1).html();
        var topic = row.children().eq(12).html();
        if ($.trim(id) === "" ) {
                    return false;
        }
        $.ajax({
            async: false,
            data: JSON.stringify({"id":id, "topic":topic, "clusterName":cluster_name}),
            url: "cockpit/manage/topic/",
            type: "DELETE",
            dataType: "json",
            contentType: "application/json",
            success: function() {
                $.ajax({
                            async: false,
                            url: "cockpit/api/topic/" + id,
                            type: "DELETE",
                            dataType: "json",
                            contentType: "application/json",
                            success: function() {
                                row.remove();
                            }
                        });
            }
        });
    });

    $(".approveItem").live("click", function() {
            var row = $(this).parent().parent();
            var id = row.children().eq(0).html();
            var cluster_name = row.children().eq(1).html();
            var permission = row.children().eq(2).html();
            var write_queue_num = row.children().eq(3).html();
            var read_queue_num = row.children().eq(4).html();
            var unit = row.children().eq(5).html();
            var has_unit_subscription = row.children().eq(6).html();
            var broker_address = row.children().eq(7).html();
            var order = row.children().eq(8).html();
            var topic = row.children().eq(12).html();
            var ob = JSON.stringify({"id":id, "topic":topic,"writeQueueNum":write_queue_num,"readQueueNum":read_queue_num,
                                     "brokerAddress":broker_address, "clusterName":cluster_name, "permission":permission,
                                     "unit":unit, "hasUnitSubscription":has_unit_subscription, "order":order});
            if ($.trim(id) === "" ) {
                        return false;
            }
            $.ajax({
                async: false,
                data: ob,
                url: "cockpit/manage/topic",
                type: "POST",
                dataType: "json",
                contentType: "application/json",
                success: function() {
                    $.ajax({
                        async: false,
                        url: "cockpit/api/topic/" + id,
                        type: "POST",
                        dataType: "json",
                        contentType: "application/json",
                        success: function() {

                                                    }
                    });
                }
            });
        });
});