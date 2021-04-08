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
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // populate movie table
    //find empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // iterate through resultData, should be 20 entries
    for (let i = 0; i < resultData.length; i++) {

        // concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] + // display movie_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_stars"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";

        // append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});