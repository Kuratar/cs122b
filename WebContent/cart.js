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
        rowHTML += "<th style=\"font-size: x-large\">" + resultArray[i]["title"] + "</th>";
        rowHTML += "<th style=\"font-size: x-large\">" + resultArray[i]["quantity"] + "</th>";
        rowHTML += "<th style=\"font-size: x-large\">" + "$" + parseInt(resultArray[i]["quantity"])*10 + ".00" + "</th>";
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