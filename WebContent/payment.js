let payment_form = $("#payment_form");

function handlePaymentResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        $("#payment_success_message").text("Your payment has been confirmed. Enjoy the movie!");
        //window.location.replace("index.html");
        console.log(resultDataJson["sale_info"]);
        console.log(resultDataJson["total"]);
         $("#sale_info").text(resultDataJson["sale_info"]);
         $("#total").text(resultDataJson["total"]);
        window.location.replace("place-order.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#payment_error_message").text(resultDataJson["message"]);

    }
}

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let total = jQuery("#payment_total");

    let quantity = 0;
    for(let i = 0; i<resultData['previousItems'].length; i++)
    {
        quantity += resultData['previousItems'][i]['quantity'];
    }

    // append two html <p> created to the h3 body, which will refresh the page
    total.append("<p>Payment Total: $" + quantity*10 + ".00</p>" + "</p>");

    console.log("handleResult: Calculated payment total from resultData");


}

function submitPaymentForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/place-order", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/payment", // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});

// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);