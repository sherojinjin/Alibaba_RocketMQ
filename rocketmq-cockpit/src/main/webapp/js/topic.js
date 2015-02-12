$(document).ready(function() {

    $.get("rocketmq/topics", function(data) {
        $(".table-content").children().remove();
        data.forEach(function(topic) {
            var incLink = $("<input type='text' class='form-control re_cluster_name' placeholder='remove_cluster_name'>");
            var operationLink = $("<a class='removeItem' href='javascript:;'>Remove</a>");
            var operation = $("<td></td>").append(operationLink);
            var inc = $("<td></td>").append(incLink);
            var item = $("<tr><td>" + topic + "</td></tr>");
            item.append(inc);
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
        if ($.trim(topic) === "") {
            return false;
        } else if ($.trim(cluster_name) === "" || $.trim(broker_address) == "") {
            return false;
        } else {
            $.ajax({
                        async: false,
                        url: "rocketmq/topic",
                        type: "POST",
                        dataType: "json",
                        contentType: 'application/json',
                        data: JSON.stringify({"topic":topic,"write_queue_num":write_queue_num,"read_queue_num":read_queue_num,
                        "broker_address":broker_address, "cluster_name":cluster_name, "order":order}),
                        success: function() {

                        },
                        error: function() {

                        }
                    });
        }
    });

    $(".removeItem").live("click", function() {
        var row = $(this).parent().parent();
        var topic = $(this).parent().prev().prev().html();
        var cluster_name = $(this).parent().prev().children("input").eq(0).val();
        if ($.trim(topic) === "" || $.trim(cluster_name) == "") {
                    return false;
        }
        $.ajax({
            async: false,
            data: {topic: topic, cluster_name: cluster_name},
            url: "rocketmq/topic",
            type: "DELETE",
            dataType: "json",
            contentType: "application/x-www-form-urlencoded",
            complete: function() {
                row.remove();
            }
        });
    });
});