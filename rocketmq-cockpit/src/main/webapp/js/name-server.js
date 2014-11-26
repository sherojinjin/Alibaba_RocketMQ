$(document).ready(function() {

    $.get("rocketmq/nsaddrJson", function(data) {
        $(".table-content").children().remove();
        data.forEach(function(nameServerItem) {
            var operationLink = $("<a class='removeItem' href='javascript:;'>Remove</a>");
            var operation = $("<td></td>").append(operationLink);
            var item = $("<tr><td>" + nameServerItem.url + "</td><td>" + nameServerItem.date + "</td></tr>");
            item.append(operation);
            $(".table-content").append(item);
        });

    });


    $(".addNameServer").click(function() {
        var nameServer = $("input.newNameServer").val();
        if ($.trim(nameServer) === "") {
            return false;
        } else {
            $.post("rocketmq/nsaddr", {nameServer: nameServer}, function() {
                $("input.newNameServer").val("");
                var item = $("<tr><td>" + nameServer + "</td><td>Just Now</td><td><a class='removeItem' href='javascript:;'>Remove</a></td></tr>");
                $(".table-content").append(item);
            });
        }
    });

    $(".removeItem").live("click", function() {
        var row = $(this).parent().parent();
        var nameServer = $(this).parent().prev().prev().html();
        $.ajax({
            async: false,
            data: {nameServer: nameServer},
            url: "rocketmq/nsaddr",
            type: "DELETE",
            dataType: "application/json",
            complete: function() {
                row.remove();
            }
        });
    });
});