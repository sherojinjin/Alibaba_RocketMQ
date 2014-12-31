$(document).ready(function() {

    $.get("rocketmq/consumerByGroupName", function(data) {
        $(".table-content").children().remove();
        data.forEach(function(ConsumerItem) {
            var item = $("<tr><td>" + ConsumerItem.name + "</td><td>" + ConsumerItem.ip + "</td></tr>");
            $(".table-content").append(item);
        });

    });


    $(".findConsumers").click(function() {
        var groupName = $("input.groupName").val();
        if ($.trim(groupName) === "") {
            return false;
        } else {
            $.post("rocketmq/consumerByGroupName", {groupName: groupName}, function(data) {
                $("input.groupName").val("");
                $(".table-content").children().remove();
                data.forEach(function(ConsumerItem) {
                    var item = $("<tr><td>" + ConsumerItem.id + "</td><td>" + ConsumerItem.ip + "</td></tr>");
                    $(".table-content").append(item);
                });
            });
        }
    });
});