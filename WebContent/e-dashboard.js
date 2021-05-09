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


function showMetadata() {
    console.log("showMetadata: setting up for servlet call");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/metadata",
        success: (resultData) => handleMetadataResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
    });
    return false;
}


function handleMetadataResult(resultData) {
    console.log("handle show metadata response");
    let databaseNameElement = jQuery("#database_name");
    if (window.document.getElementById("database_name").textContent === "") {
        databaseNameElement.append("<p>Database: moviedbexample</p>");
    } else { return; }

    let dbTablesElement = jQuery("#db_tables");
    for (let i = 0; i < resultData.length; i++) {
        let tableHTML = "<pre>";
        // name of table
        tableHTML += resultData[i]["name"] + "";
        // get array of attributes and store into variable
        let attributes = resultData[i]["attributes"];
        console.log(attributes);
        for (let j = 0; j < attributes.length; j++) {
            // each line under table name = "    field: type"
            tableHTML += "\n    " + attributes[j]["field"] + " " + attributes[j]["type"];
        }
        tableHTML += "</pre>";
        dbTablesElement.append(tableHTML);
    }
}


function insertStar() {
    console.log("insertStar function: setting up for servlet call");
    let star_form = $("#star_form");
    let starName = getParameterByName("name");
    let birthYear = getParameterByName("birth_year");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
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
