let payment_form = $("#payment_form");
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
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            //success: system.out.println("works lol");
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