/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Star Name: " + resultData[0]["star_name"] + "</p>" +
        "<p>Date Of Birth: " + resultData[0]["star_dob"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th style='font-size: x-large'>" + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] +
            '&list=' + movieListType +
            '&title=' + title +
            '&year=' + year +
            '&director=' + director +
            '&star=' + star +
            '&genreId=' + genreId +
            '&char=' + char +
            '&query=' + query +
            '&nMovies=' + nMovies +
            '&page=' + page +
            '&sorting=' + sortingOption + '">'
            + resultData[i]["movie_title"] + // display movie_name for the link text
            '</a>' + "</th>";
        rowHTML += "<th style='font-size: x-large'>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th style='font-size: x-large'>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }

    // create back button to movie list
    let backMovieListElement = jQuery("#back_movie_list");
    switch (movieListType) {
        case "search":
            backMovieListElement.append('<a href="search.html?title=' + title +
            '&year=' + year +
            '&director=' + director +
            '&star=' + star +
            '&nMovies=' + nMovies +
            '&page=' + page +
            '&sorting=' + sortingOption + '">' +
            'Back to Movie List' + '</a>')
            break;
        case "browseGenre":
            backMovieListElement.append('<a href="browse-genre.html?id=' + genreId +
            '&nMovies=' + nMovies +
            '&page=' + page +
            '&sorting=' + sortingOption + '">' +
                'Back to Movie List' + '</a>')
            break;
        case "browseTitle":
            backMovieListElement.append('<a href="browse-title.html?char=' + char +
            '&nMovies=' + nMovies +
            '&page=' + page +
            '&sorting=' + sortingOption + '">' +
            'Back to Movie List' + '</a>')
            break;
        case "autoSearch":
            backMovieListElement.append('<a href="auto-search.html?query=' + query +
            '&nMovies=' + nMovies +
            '&page=' + page +
            '&sorting=' + sortingOption + '">' +
            'Back to Movie List' + '</a>')
            break;
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');
let movieListType = getParameterByName('list');
let nMovies = getParameterByName('nMovies');
let page = getParameterByName('page');
let sortingOption = getParameterByName('sorting');
// possible movie list parameters
let title = getParameterByName('title');
let year = getParameterByName('year');
let director = getParameterByName('director');
let star = getParameterByName('star');
let genreId = getParameterByName('genreId');
let char = getParameterByName('char');
let query = getParameterByName('query');

if (movieListType === "search") {
    // Makes the HTTP GET request and registers on success callback function handleResult
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/single-star?id=" + starId + "&list=" + movieListType +
        "&title=" + title + "&year=" + year + "&director=" + director + "&star=" + star +
        "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sortingOption, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}
else if (movieListType === "browseGenre") {
    // Makes the HTTP GET request and registers on success callback function handleResult
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/single-star?id=" + starId + "&list=" + movieListType + "&genreId=" + genreId +
            "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sortingOption, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}
else if (movieListType === "browseTitle") {
    // Makes the HTTP GET request and registers on success callback function handleResult
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/single-star?id=" + starId + "&list=" + movieListType + "&char=" + char +
            "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sortingOption, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}
else if (movieListType === "autoSearch") {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/single-star?id=" + starId + "&list=" + movieListType + "&query=" + query +
            "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sortingOption, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleResult(resultData), // Setting callback function to handle data returned successfully by the MoviesServlet
    });
}
