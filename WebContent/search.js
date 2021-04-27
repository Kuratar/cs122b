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
 * When user searches a movie from the main page (index.html), the default parameters
 * do not exist and this functions add them without refreshing the page.
 */
function addDefaultParameters() {
    let nMoviesParam = getParameterByName("nMovies");
    let pageParam = getParameterByName("page");
    let sortingOptionParam = getParameterByName("sorting");
    if (nMoviesParam == null && pageParam == null && sortingOptionParam == null) {
        console.log("Adding default parameters to url");
        window.history.pushState(null, null,
            window.location.href + "&nMovies=10&page=0&sorting=default");
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/search?title=" + title + "&year=" + year + "&director=" + director + "&star=" + star +
                "&nMovies=10&page=0&sorting=default", // Setting request url, which is mapped by MoviesServlet in Movies.java
            success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
        });
    }
}

function changeSortingOption() {
    let option = document.getElementById("sortingOption").value;
    if (option !== sorting) {
        location.search = location.search.replace(/sorting=[^&$]*/i, 'sorting='+option);
    }
    return false;
}

function changeNMovies() {
    let option = document.getElementById("nMoviesOption").value;
    if (option !== nMovies) {
        location.search = location.search.replace(/nMovies=[^&$]*/i, 'nMovies='+option);
    }
    return false;
}

function addToShoppingCart(movieId, rowMessageId)
{
    console.log("Added movie to shopping cart");
    let addedToCartLabel = jQuery("#" + rowMessageId);
    let addedToCartMessage = window.document.getElementById(rowMessageId).textContent;

    $.ajax("api/cart", {
        method: "POST",
        data: {id: movieId},
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
                window.document.getElementById(rowMessageId).textContent = addedToCartMessage;
            }
        }
    });
}

function handleSearchResult(resultData) {
    if (resultData.length === 0) {
        let prevButtonElementTop = jQuery("#prev_link_top");
        let noMoreMoviesElementTop = jQuery("#no_more_movies_top");
        noMoreMoviesElementTop.append("<b>No more movies or try changing movies per page/sorting option on the first page</b>");
        let prevButtonLink = '<a href="search.html?title=' + title +
            '&year=' + year +
            '&director=' + director +
            '&star=' + star +
            '&nMovies=' + nMovies +
            '&page=' + "0" +
            '&sorting=' + sorting + '">' +
            'Click here to go back to the first page with your desired settings' + '</a>';
        prevButtonElementTop.append(prevButtonLink);
        return;
    }

    console.log("handleSearchResult: populating search table from resultData");

    let sortingElement = jQuery("#current_sorting");
    console.log(sorting);
    switch (sorting) {
        case "default":
            sortingElement.append("Currently sorted by Default");                                       break;
        case "titleRatingASCE":
            sortingElement.append("Currently sorted by Title (ascending) with Rating to break ties");   break;
        case "titleRatingDESC":
            sortingElement.append("Currently sorted by Title (descending) with Rating to break ties");  break;
        case "ratingTitleASCE":
            sortingElement.append("Currently sorted by Rating (ascending) with Title to break ties");   break;
        case "ratingTitleDESC":
            sortingElement.append("Currently sorted by Rating (descending) with Title to break ties");  break;
    }

    let nMoviesElement = jQuery("#current_nMovies");
    nMoviesElement.append("Currently " + nMovies + " movies per page")

    // populate genre table
    // find empty table body by id "genre_table_body"
    let searchTableBodyElement = jQuery("#movie_table_body");

    // iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th style=\"font-size: x-large\">" +
            // add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] +
            '&list=search' +
            '&title=' + title +
            '&year=' + year +
            '&director=' + director +
            '&star=' + star +
            '&nMovies=' + nMovies +
            '&page=' + page +
            '&sorting=' + sorting + '">'
            + resultData[i]["movie_title"] + // display movie_name for the link text
            '</a>' +
            "</th>";

        rowHTML += "<th style=\"font-size: x-large\">" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th style=\"font-size: x-large\">" + resultData[i]["movie_director"] + "</th>";

        // add genres in one column with links
        let genreIdsSplit = resultData[i]["genre_ids"].split(", ");
        let genresSplit = resultData[i]["genre_names"].split(", ");
        let genreHTML = "";
        genreHTML += "<th style=\"font-size: x-large\">";
        for (let j = 0; j < genresSplit.length; j++) {
            genreHTML +=
                // add a link to browse-genre.html with id, nMovies, page passed with GET url parameter
                '<a href="browse-genre.html?id=' + genreIdsSplit[j] +
                '&nMovies=10&page=0&sorting=default">'
                + genresSplit[j] + // display star_name for the link text
                '</a>' + ", ";
        }
        genreHTML = genreHTML.slice(0, -2);
        genreHTML += "</th>";
        rowHTML += genreHTML;

        // add stars in one column with links
        let starsIdsSplit = resultData[i]["star_ids"].split(", ");
        let starsSplit = resultData[i]["star_names"].split(", ");
        let starHTML = "";
        starHTML += "<th style=\"font-size: x-large\">";
        for (let j = 0; j < starsSplit.length; j++) {
            starHTML +=
                // add a link to single-star.html with id passed with GET url parameter
                '<a href="single-star.html?id=' + starsIdsSplit[j] +
                '&list=search' +
                '&title=' + title +
                '&year=' + year +
                '&director=' + director +
                '&star=' + star +
                '&nMovies=' + nMovies +
                '&page=' + page +
                '&sorting=' + sorting + '">'
                + starsSplit[j] + // display star_name for the link text
                '</a>' + ", ";
        }
        starHTML = starHTML.slice(0, -2);
        starHTML += "</th>";
        rowHTML += starHTML;

        rowHTML += "<th style=\"font-size: x-large\">" + resultData[i]["movie_rating"] + "</th>";

        rowHTML += "<th style='font-size: x-large'>" +
                        "<button style=\"font-size: x-large\" onclick=\"addToShoppingCart('" + resultData[i]['movie_id'] + "', '" +
                        "added_to_cart" + i + "')\"> Add to Cart </button>\n" +
                        "<br>\n" +
                        "<label style=\"font-size: x-large\" id=\"added_to_cart" + i + "\"></label>\n" +
                   "</th>";

        rowHTML += "</tr>";

        // append the row created to the table body, which will refresh the page
        searchTableBodyElement.append(rowHTML);
    }
    // page does not equal 0, add prev button
    console.log(page);
    if (page !== '0') {
        let prevPage = parseInt(page);
        prevPage -= 1;
        let prevButtonElementTop = jQuery("#prev_link_top");
        let prevButtonElementBot = jQuery("#prev_link_bot");
        let prevButtonLink = '<a href="search.html?title=' + title +
            '&year=' + year +
            '&director=' + director +
            '&star=' + star +
            '&nMovies=' + nMovies +
            '&page=' + prevPage +
            '&sorting=' + sorting + '">' +
            'Prev' + '</a>';
        prevButtonElementTop.append(prevButtonLink);
        prevButtonElementBot.append(prevButtonLink);
    }

    // if page has # of movies = to nMovies, add next button
    console.log(nMovies);
    console.log(resultData.length);
    if (resultData.length === parseInt(nMovies)) {
        let nextPage = parseInt(page);
        nextPage += 1;
        let nextButtonElementTop = jQuery("#next_link_top");
        let nextButtonElementBot = jQuery("#next_link_bot");
        let nextButtonLink = '<a href="search.html?title=' + title +
            '&year=' + year +
            '&director=' + director +
            '&star=' + star +
            '&nMovies=' + nMovies +
            '&page=' + nextPage +
            '&sorting=' + sorting + '">' +
            'Next' + '</a>';
        nextButtonElementTop.append(nextButtonLink);
        nextButtonElementBot.append(nextButtonLink);
    }
}

// the default parameters nMovies, page, and sorting only if they are not there
// these parameters are not included initially when redirected from main page (index.html)
// which is where users first initially enter search keywords

let title = getParameterByName("title");
let year = getParameterByName("year");
let director = getParameterByName("director");
let star = getParameterByName("star");
addDefaultParameters();
let nMovies = getParameterByName("nMovies");
let page = getParameterByName("page");
let sorting = getParameterByName("sorting");

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/search?title=" + title + "&year=" + year + "&director=" + director + "&star=" + star +
         "&nMovies=" + nMovies + "&page=" + page + "&sorting=" + sorting, // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});
