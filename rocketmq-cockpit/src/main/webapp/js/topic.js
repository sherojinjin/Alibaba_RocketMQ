$(document).ready(function() {

    $.get("cockpit/api/topic", function(data) {
        $(".table-content").children().remove();
        data.forEach(function(topic) {
            var operationLink = $("<a class='removeItem' href='javascript:;'>Remove</a>");
            var approveLink = $("<a class='approveItem' href='javascript:;'>Approve</a>");

            var operation = $("<td></td>");
            if (topic.status_id === 1){
                operation.append(approveLink);
                operation.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            operation.append(operationLink);
            var item = $("<tr><td style='display:none'>" + topic.id + "</td><td style='display:none'>" + topic.cluster_name + "</td><td style='display:none'>" + topic.permission + "</td><td style='display:none'>" + topic.write_queue_num + "</td><td style='display:none'>" + topic.read_queue_num + "</td><td style='display:none'>" + topic.unit + "</td><td style='display:none'>" + topic.has_unit_subscription + "</td><td style='display:none'>" + topic.broker_address + "</td><td style='display:none'>" + topic.order_type + "</td><td style='display:none'>" + topic.status_id + "</td><td style='display:none'>" + topic.create_time + "</td><td style='display:none'>" + topic.update_time + "</td><td>" + topic.topic + "</td></tr>");
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
        var allow = 1;
        var ob = JSON.stringify({"topic":topic,"writeQueueNum":write_queue_num,"readQueueNum":read_queue_num,
                                 "broker_address":broker_address, "cluster_name":cluster_name, "permission":permission, "unit":unit, "hasUnitSubscription":has_unit_subscription, "order":order, "status":allow});
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
            data: JSON.stringify({"id":id, "topic":topic, "cluster_name":cluster_name}),
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
                            complete: function() {
                                row.remove();
                            }
                        });
                row.remove();
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
            var order_type = row.children().eq(8).html();
            var status_id = row.children().eq(9).html();
            var create_time = row.children().eq(10).html();
            var update_time = row.children().eq(11).html();
            var topic = row.children().eq(12).html();
            var ob = JSON.stringify({"id":id, "topic":topic,"writeQueueNum":write_queue_num,"readQueueNum":read_queue_num,
                                     "broker_address":broker_address, "cluster_name":cluster_name, "permission":permission,
                                     "unit":unit, "hasUnitSubscription":has_unit_subscription, "order":order, "status":2});
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
                        data: ob,
                        url: "cockpit/api/topic",
                        type: "POST",
                        dataType: "json",
                        contentType: "application/json",
                        complete: function() {
                                                       location.reload(true);
                                                    }
                    });
                }
            });
        });
});