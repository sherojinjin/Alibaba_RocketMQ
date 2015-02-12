$(document).ready(function() {

    $.get("rocketmq/ips", function(data) {
        $(".table-content").children().remove();
        data.forEach(function(ip) {
            var operationLink = $("<a class='removeItem' href='javascript:;'>Remove</a>");
            var operation = $("<td></td>").append(operationLink);
            var item = $("<tr><td>" + ip.innerIP + "</td><td>" + ip.publicIP + "</td></tr>");
            item.append(operation);
            $(".table-content").append(item);
        });
    });


    $(".addMapping").click(function() {
        var innerIP = $("input.innerIP").val();
        var publicIP = $("input.publicIP").val();
        if ($.trim(innerIP) === "" || $.trim(publicIP) == "") {
            return false;
        } else {
            $.post("rocketmq/ip", {innerIP: innerIP, publicIP:publicIP}, function() {
                $("input.innerIP").val("");
                $("input.publicIP").val("");
                var item = $("<tr><td>" + innerIP + "</td><td>" + publicIP + "</td><td><a class='removeItem' href='javascript:;'>Remove</a></td></tr>");
                $(".table-content").append(item);
            });
        }
    });

    $(".removeItem").live("click", function() {
        var row = $(this).parent().parent();
        var innerIP = $(this).parent().prev().prev().html();
        $.ajax({
            async: false,
            data: JSON.stringify({inner_ip: innerIP}),
            url: "rocketmq/ip",
            type: "DELETE",
            dataType: "application/json",
            contentType: "application/json",
            complete: function() {
                row.remove();
            }
        });
    });
});