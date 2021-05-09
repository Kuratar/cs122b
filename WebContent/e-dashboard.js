/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function insertStar() {
    let star_form = $("#star_form");
    let starName = getParameterByName("name");
    let birthYear = getParameterByName("birth_year");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        //url: "api/insert-star?name=" + starName + "&birthYear=" + birthYear, // Setting request url, which is mapped by MoviesServlet in Movies.java
        url: "api/insert-star?" + star_form.serialize(),
        success: (resultData) => handleInsertStarResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
    });

    return false;
}


function handleInsertStarResult(resultData) {
    console.log("handle insert star response");
    console.log(resultData);
    console.log(resultData["status"]);

    if (resultData["status"] === "success") {
        $("#star_form_message").text(resultData["message"]);
    }
    else {
        $("#star_form_message").text("Error adding new star");
        console.log(resultData["errorMessage"]);
    }
}


function insertMovie() {

    return false;
}


function showMetadata() {

    return false;
}