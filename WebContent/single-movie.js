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
function addToShoppingCart()
{
    console.log("Added movie to shopping cart");
    let addedToCartLabel = jQuery("#added_to_cart");
    let addedToCartMessage = window.document.getElementById("added_to_cart").textContent;

    $.ajax("api/cart", {
        method: "POST",
        data: {id: getParameterByName('id')},
        error: function() {
            console.log("Failure: add to cart");
            if (addedToCartMessage !== "Failure: add to cart") {
                addedToCartLabel.append("Failure: could not add to cart");
            }
        },
        success: function () {
            console.log("Success: add to cart");
            if (addedToCartMessage === "") {
                addedToCartLabel.append("Added to cart 1");
            }
            else {
                let addedToCartNumber = parseInt(addedToCartMessage.slice(14))+1;
                addedToCartMessage = "Added to cart " + addedToCartNumber;
                window.document.getElementById("added_to_cart").textContent = addedToCartMessage;
            }
        }
    });
}
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

    console.log("handleResult: populating movie info from resultData");

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    let genreIds = resultData[0]["movie_genreIds"];
    let genreNames = resultData[0]["movie_genreNames"];
    console.log(genreIds);
    console.log(genreNames);
    let genreIdsSplit = genreIds.split(", ");
    let genreNamesSplit = genreNames.split(", ");
    console.log(genreIdsSplit);
    console.log(genreNamesSplit);
    let genreHTML = "<p>Genre(s): ";
    for (let i = 0; i < genreIdsSplit.length; i++) {
        genreHTML +=
            // add a link to browse-genre.html with id, nMovies, page passed with GET url parameter
            '<a href="browse-genre.html?id=' + genreIdsSplit[i] +
            '&nMovies=' + nMovies +
            '&page=0' +
            '&sorting=' + sortingOption + '">' +
            genreNamesSplit[i] + // display star_name for the link text
            '</a>' + ", ";
    }
    genreHTML = genreHTML.slice(0, -2);
    genreHTML += "</p>";

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Movie Title: " + resultData[0]["movie_title"] + "</p>" +
        "<p>Release Year: " + resultData[0]["movie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movie_director"] + "</p>" +
        genreHTML +
        "<p>Rating: " + resultData[0]["movie_rating"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#star_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let starIds = resultData[0]["movie_starIds"];
    let starNames = resultData[0]["movie_starNames"];
    let starIdsSplit = starIds.split(", ");
    let starNamesSplit = starNames.split(", ");
    for (let i = 0; i < starIdsSplit.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th style='font-size: x-large'>" +
            // add a link to single-star.html with id passed with GET url parameter
            '<a href="single-star.html?id=' + starIdsSplit[i] +
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
            + starNamesSplit[i] + // display star_name for the link text
            '</a>' +
            "</th>";
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
let movieId = getParameterByName('id');
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
        url: "api/single-movie?id=" + movieId + "&list=" + movieListType +
            "&title=" + title + "&year=" + year + "&director=" + director + "&star=" + star +
            "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sortingOption, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
    });
}
else if (movieListType === "browseGenre") {
    // Makes the HTTP GET request and registers on success callback function handleResult
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/single-movie?id=" + movieId + "&list=" + movieListType + "&genreId=" + genreId +
            "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sortingOption, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
    });
}
else if (movieListType === "browseTitle") {
    // Makes the HTTP GET request and registers on success callback function handleResult
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/single-movie?id=" + movieId + "&list=" + movieListType + "&char=" + char +
            "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sortingOption, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
    });
}
else if (movieListType === "autoSearch") {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/single-movie?id=" + movieId + "&list=" + movieListType + "&query=" + query +
            "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sortingOption, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleResult(resultData), // Setting callback function to handle data returned successfully by the MoviesServlet
    });
}
