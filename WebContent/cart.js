function proceedToPayment()
{
    console.log("Proceeding to payment for shopping cart");
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/payment",
        success: handleResult
    });
}

function decreaseQuantity(movieTitle) {
    $.ajax("api/modify-cart", {
        method: "POST",
        data: {title: movieTitle, command: "decrease"}
    });
}
function increaseQuantity(movieTitle)
{
    $.ajax("api/modify-cart", {
        method: "POST",
        data: {title: movieTitle, command: "increase"}
    });
}

function deleteItem(movieTitle)
{
    $.ajax("api/modify-cart", {
        method: "POST",
        data: {title: movieTitle, command: "delete"}
    });
}

function handleSessionData(resultDataString) {
    console.log("handle session response");
    console.log(resultDataString);
    console.log(resultDataString["sessionID"]);

    // show the session information
    $("#sessionID").text("Session ID: " + resultDataString["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataString["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataString["previousItems"]);
}

function handleCartArray(resultArray) {
    console.log(resultArray);
    let cart_list = jQuery("#cart_list");
    // change it to html list
    let rowHTML = "";
    for (let i = 0; i < resultArray.length; i++) {
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultArray[i]["title"] + "</th>";
        rowHTML += "<th>" + resultArray[i]["quantity"] + "</th>";
        rowHTML += "<th>" + "$10.00" + "</th>";
        rowHTML += "<th style='font-size: x-large'>" +
            "<button style=\"font-size: x-large\" onclick=\"decreaseQuantity('" + resultArray[i]['title'] + "', '" +
            "added_to_cart" + i + "')\"> Decrease Quantity </button>\n" +
            "<br>\n" +
            "<label style=\"font-size: x-large\" id=\"decrease_cart" + i + "\"></label>\n" +
            "</th>";
        rowHTML += "<th style='font-size: x-large'>" +
            "<button style=\"font-size: x-large\" onclick=\"increaseQuantity('" + resultArray[i]['title'] + "', '" +
            "added_to_cart" + i + "')\"> Increase Quantity </button>\n" +
            "<br>\n" +
            "<label style=\"font-size: x-large\" id=\"increase_cart" + i + "\"></label>\n" +
            "</th>";
        rowHTML += "<th style='font-size: x-large'>" +
            "<button style=\"font-size: x-large\" onclick=\"deleteItem('" + resultArray[i]['title'] + "', '" +
            "added_to_cart" + i + "')\"> Delete Item </button>\n" +
            "<br>\n" +
            "<label style=\"font-size: x-large\" id=\"delete_from_cart" + i + "\"></label>\n" +
            "</th>";
        rowHTML += "</tr>";

    }

    // clear the old array and show the new array in the frontend
    cart_list.html("");
    cart_list.append(rowHTML);
}

$.ajax("api/cart", {
    method: "GET",
    success: handleSessionData
});