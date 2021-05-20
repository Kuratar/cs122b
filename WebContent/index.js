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
            '<a href="browse-genre.html?id=' + resultData[i]['genre_id'] +
            '&nMovies=10&page=0&sorting=default">'
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
            '<a href="browse-title.html?char=' + firstChars[i] +
            '&nMovies=10&page=0&sorting=default">' +
            firstChars[i] + // display char for the link text
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
        '<a href="browse-title.html?char=' + '*' +
        '&nMovies=10&page=0&sorting=default">' +
        "* (Non-alphanumerical characters)" +
        '</a>' +
        "</th>";
    lastRowHTML += "</tr>";

    // append the row created to the table body, which will refresh the page
    titleTableBodyElement.append(lastRowHTML);
}

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here
    window.location.assign("auto-search.html?query=" + query + "&nMovies=10&page=0&sorting=default");
}

function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    let movieTitle = suggestion["value"]
    let movieId = suggestion["data"]["movieId"]
    console.log("user selected " + movieTitle + " with ID " + movieId)
    window.location.assign("single-movie.html?id=" + movieId + "&list=autoSearch&query=" + movieTitle +
                            "&nMovies=10&page=0&sorting=default")
}

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated");
    // TODO: if you want to check past query results first, you can do it here
    if (prevQuery.includes(query)) {
        console.log("new query \"" + query + "\" similar to previous query \"" + prevQuery + "\"")
        console.log("suggesting previous suggestions from cache")
        doneCallback( { suggestions: prevSuggestions } );
        return;
    }
    prevQuery = query;
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    console.log("sending AJAX request to backend Java Servlet");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/auto-search?input=" + query, // Setting request url, which is mapped by MoviesServlet in Movies.java
        success: function(resultData) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(resultData, query, doneCallback);
        },
        error: function(resultData) {
            console.log("error from auto search servlet");
            console.log("error: " + resultData[0]["errorMessage"]);
        }
    });


}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")
    console.log("sending suggestion results and caching for duplicate queries")
    console.log(data)

    // TODO: if you want to cache the result into a global variable you can do it here
    prevSuggestions = data;

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}

function autoSearchButton() {
    console.log("user clicked autocomplete search button");
    handleNormalSearch($('#autocomplete').val());
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode === 13) {
        console.log("user pressed enter key on autocomplete search");
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3
});

let prevSuggestions;
let prevQuery = "";

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

