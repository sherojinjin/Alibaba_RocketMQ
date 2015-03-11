$(document).ready(function() {
    $.get("cockpit/ajax/name-server/kv", function(data) {
        $(".table-content").children().remove();

        data.forEach(function(item) {
            var operationLink = $("<a href='javascript:;' rel='" + item.id + "' class='applyKV'>Apply</a>");
            var operation = $("<td></td>").append(operationLink);
            var item = $("<tr><td>" + item.nameSpace + "</td><td>" + item.key + "</td><td>" + item.value + "</td><td>"
            + item.status + "</td></tr>");
            item.append(operation);
            $(".table-content").append(item);

        });
    });

    $(".addKV").click(function() {
        var nameSpace = $(".nameSpace").val();
        var key = $(".key").val();
        var value = $(".value").val();

        $.ajax({
            url: "cockpit/ajax/name-server/kv",
            type: "PUT",
            data: {nameSpace: nameSpace, key: key, value: value},
            success: function(data) {
                var operationLink = $("<a href='javascript:;' rel='" + data.id  + "' class='applyKV'>Apply</a>");
                var operation = $("<td></td>").append(operationLink);
                var item = $("<tr><td>" + nameSpace + "</td><td>" + key + "</td><td>" + value + "</td><td>"
                + data.status + "</td></tr>");
                item.append(operation);
                $(".table-content").append(item);

                $(".nameSpace").val("");
                $(".key").val("");
                $(".value").val("");
            },
            error: function() {
                alert("Adding KV fails.");
            }
        });
    });

});