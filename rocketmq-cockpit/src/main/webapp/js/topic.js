$(document).ready(function() {

    $.get("rocketmq/topics", function(data) {
        $(".table-content").children().remove();
        data.forEach(function(topic) {
            var operationLink = $("<a class='removeItem' href='javascript:;'>Remove</a>");
            var approveLink = $("<a class='approveItem' href='javascript:;'>Approve</a>");

            var operation = $("<td></td>");
            if (!topic.allow){
                operation.append(approveLink);
                operation.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            operation.append(operationLink);
            var item = $("<tr><td style='display:none'>" + topic.id + "</td><td>" + topic.topic + "</td></tr>");
            item.append(operation);
            $(".table-content").append(item);
        });
    });

    $(".addTopic").click(function() {
        var topic = $("input.topic").val();
        var write_queue_num = $("input.write_queue_num").val();
        var read_queue_num = $("input.read_queue_num").val();
        var broker_address = $("input.broker_address").val();
        var cluster_name = $("input.cluster_name").val();
        var order = $("input.order").val();
        var allow = false;
        if ($.trim(topic) === "") {
            return false;
        } else if ($.trim(cluster_name) === "" && $.trim(broker_address) == "") {
            return false;
        } else {
            $.ajax({
                        async: false,
                        url: "rocketmq/topic",
                        type: "POST",
                        dataType: "json",
                        contentType: 'application/json',
                        data: JSON.stringify({"topic":topic,"write_queue_num":write_queue_num,"read_queue_num":read_queue_num,
                        "broker_address":broker_address, "cluster_name":cluster_name, "order":order, "allow":allow}),
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
        if ($.trim(id) === "" ) {
                    return false;
        }
        $.ajax({
            async: false,
            data: JSON.stringify({id: id}),
            url: "rocketmq/topic",
            type: "DELETE",
            dataType: "json",
            contentType: "application/json",
            complete: function() {
                row.remove();
            }
        });
    });

    $(".approveItem").live("click", function() {
            var row = $(this).parent().parent();
            var id = row.children().eq(0).html();
            if ($.trim(id) === "" ) {
                        return false;
            }
            $.ajax({
                async: false,
                data: JSON.stringify({id: id}),
                url: "rocketmq/topic",
                type: "PUT",
                dataType: "json",
                contentType: "application/json",
                complete: function() {
                    location.reload(true);
                }
            });
        });
});