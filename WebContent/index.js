/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleGenresResult(resultData) {
    console.log("handleGenresResult: populating genre table from resultData");

    // populate genre table
    // find empty table body by id "genre_table_body"
    let genreTableBodyElement = jQuery("#genre_table_body");

    // iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th style='font-size: x-large'>" +
            // add a link to browse-genre.html with id passed with GET url parameter
            '<a href="browse-genre.html?id=' + resultData[i]['genre_id'] + '&nMovies=25&page=0">'
            + resultData[i]["genre_name"] + // display genre_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "</tr>";

        // append the row created to the table body, which will refresh the page
        genreTableBodyElement.append(rowHTML);
    }
}


function handleTitles() {
    console.log("handleTitles: populating title table from resultData");

    // populate title table
    // find empty table body by id "title_table_body"
    let titleTableBodyElement = jQuery("#title_table_body");

    let firstChars = ["0","1","2","3","4","5","6","7","8","9",
                      "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"];

    // iterate through first chars of titles
    for (let i = 0; i < firstChars.length; i++) {
        // concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th style='font-size: x-large'>" +
            // add a link to browse-title.html with id passed with GET url parameter
            '<a href="browse-title.html?char=' + firstChars[i] + '&nMovies=25">'
            + firstChars[i] + // display char for the link text
            '</a>' +
            "</th>";
        rowHTML += "</tr>";

        // append the row created to the table body, which will refresh the page
        titleTableBodyElement.append(rowHTML);
    }

    // add * to titles
    let lastRowHTML = "";
    lastRowHTML += "<tr>";
    lastRowHTML +=
        "<th style='font-size: x-large'>" +
        // add a link to browse-title.html with id passed with GET url parameter
        '<a href="browse-title.html?char=' + '*' + '&nMovies=25">' +
        "* (Non-alphanumerical characters)" +
        '</a>' +
        "</th>";
    lastRowHTML += "</tr>";

    // append the row created to the table body, which will refresh the page
    titleTableBodyElement.append(lastRowHTML);
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres", // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleGenresResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});

handleTitles();