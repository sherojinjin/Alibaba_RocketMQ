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


    $(".applyKV").on('click', function() {
        alert("Apply Clicked");
    });

});