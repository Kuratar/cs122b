import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

@WebServlet(name = "BrowseByTitleServlet", urlPatterns = "/api/browse-title")
public class BrowseByTitleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String currentNMovies;
    private String currentPageNumber;
    private String currentSortingOption;
    // holds the next page of results
    private JsonArray nextPageResults;


    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        // initialize current page number to 0 for first call of class
        currentNMovies = "";
        currentPageNumber = "";
        currentSortingOption = "";
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private JsonArray generateResults(HttpServletResponse response, Connection conn,
                                      String firstChar, String nMovies, String pageNumber, String sortingOption)
    throws  IOException {
        // if first character is asterisk
        String query = "";
        if (firstChar.equals("*")) {
            // use this query to get all non alphanumeric characters
            query = "SELECT movies.id, title, year, director, rating \n" +
                    "from ratings, movies \n" +
                    "where title regexp '^[^a-zA-Z0-9].*' and ratings.movieId = movies.id\n";
        }
        // other wise
        else {
            // add first character normally to string query
            query = "SELECT movies.id, title, year, director, rating \n" +
                    "from ratings, movies \n" +
                    "where title like ? and " +
                    "ratings.movieId = movies.id\n";
        }
        // sorting option if given
        switch (sortingOption) {
            case "titleRatingASCE": query += "order by title, rating\n";        break;
            case "titleRatingDESC": query += "order by title desc, rating\n";   break;
            case "ratingTitleASCE": query += "order by rating, title\n";        break;
            case "ratingTitleDESC": query += "order by rating desc, title\n";   break;
        }
        // add pagination parameters
        query += "limit ?\n" +
                "offset ?";

        // query to get first 3 genres based on alphabetical order
        String query2 = "select genres.id, name\n" +
                "from genres_in_movies, genres\n" +
                "where movieId=? and genres.id = genres_in_movies.genreId\n" +
                "order by name\n" +
                "limit 3";

        // query to get first 3 stars based on number of movies starred
        String query3 = "select s.id, s.name, count(movieId) as movies\n" +
                "from (\n" +
                "select stars.id, name\n" +
                "from stars_in_movies, stars\n" +
                "where movieId=? and stars.id = stars_in_movies.starId\n" +
                "order by id\n" +
                ") as s, stars_in_movies\n" +
                "where s.id = stars_in_movies.starId\n" +
                "group by s.id\n" +
                "order by movies desc\n" +
                "limit 3";

        try (PreparedStatement statement = conn.prepareStatement(query);
             PreparedStatement statement2 = conn.prepareStatement(query2);
             PreparedStatement statement3 = conn.prepareStatement(query3)) {
            JsonArray jsonArray = new JsonArray();

            // if firstChar is asterisk
            if (firstChar.equals("*")) {
                // place in number of movies per page
                statement.setInt(1, Integer.parseInt(nMovies));
                // place in offset for which page of results
                statement.setInt(2, Integer.parseInt(pageNumber) * Integer.parseInt(nMovies));
            }
            // otherwise
            else {
                statement.setString(1, firstChar + '%');
                // place in number of movies per page
                statement.setInt(2, Integer.parseInt(nMovies));
                // place in offset for which page of results
                statement.setInt(3, Integer.parseInt(pageNumber) * Integer.parseInt(nMovies));
            }
            System.out.println(statement.toString());

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                // get properties for movie
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating");

                // set the movieId for getting genre and star queries
                statement2.setString(1, movieId);
                statement3.setString(1, movieId);

                ResultSet rs2 = statement2.executeQuery();
                ResultSet rs3 = statement3.executeQuery();

                // get ids and names of genre and stars into a string
                String genreIds = "";
                String genreNames = "";
                String starIds = "";
                String starNames = "";
                for (int j = 0; j < 3; j++) {
                    if (rs2.next()) {
                        genreIds += rs2.getString("id") + ", ";
                        genreNames += rs2.getString("name") + ", ";
                    }
                    if (rs3.next()) {
                        starIds += rs3.getString("id") + ", ";
                        starNames += rs3.getString("name") + ", ";
                    }
                }
                // remove last comma and space
                genreIds = genreIds.substring(0, genreIds.length()-2);
                genreNames = genreNames.substring(0, genreNames.length()-2);
                starIds = starIds.substring(0, starIds.length()-2);
                starNames = starNames.substring(0, starNames.length()-2);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonObject.addProperty("genre_ids", genreIds);
                jsonObject.addProperty("genre_names", genreNames);
                jsonObject.addProperty("star_ids", starIds);
                jsonObject.addProperty("star_names", starNames);
                jsonArray.add(jsonObject);

                rs2.close();
                rs3.close();
            }

            rs.close();
            statement.close();
            statement2.close();
            statement3.close();
            return jsonArray;
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            JsonArray errorArray = new JsonArray();
            jsonObject.addProperty("errorMessage", e.getMessage() + "from generateResults");
            errorArray.add(jsonObject);

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
            return errorArray;
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");

        String firstChar = request.getParameter("char");
        String nMovies = request.getParameter("nMovies");
        String pageNumber = request.getParameter("page");
        String sortingOption = request.getParameter("sorting");

        try (Connection conn = dataSource.getConnection()) {
            // if the page is an even number, always generate the current page of results and the next
            if (Integer.parseInt(pageNumber) % 2 == 0) {
                JsonArray jsonArray = generateResults(response, conn, firstChar, nMovies, pageNumber, sortingOption);
                response.getWriter().write(jsonArray.toString());
                JsonArray nextPageArray = generateResults(response, conn, firstChar, nMovies, Integer.toString(Integer.parseInt(pageNumber)+1), sortingOption);
                currentNMovies = nMovies;
                currentPageNumber = pageNumber;
                currentSortingOption = sortingOption;
                nextPageResults = nextPageArray;
            }
            // if the page number is an odd number
            else if (Integer.parseInt(pageNumber) % 2 == 1) {
                // if the page number is the next page,
                if (Integer.parseInt(pageNumber) == Integer.parseInt(currentPageNumber)+1) {
                    // if the settings are not the same, generate new nextPageResults and send
                    if (!currentNMovies.equals(nMovies) || !currentSortingOption.equals(sortingOption)) {
                        nextPageResults = generateResults(response, conn, firstChar, nMovies, pageNumber, sortingOption);
                        currentNMovies = nMovies;
                        currentSortingOption = sortingOption;
                    }
                    // statement reached if settings are same thus send nextPage
                    currentPageNumber = pageNumber;
                    response.getWriter().write(nextPageResults.toString());
                }
                // if the page number is not the next page, generate results for previous page and send
                else {
                    JsonArray previousPageResults = generateResults(response, conn, firstChar, nMovies, pageNumber, sortingOption);
                    currentNMovies = nMovies;
                    currentSortingOption = sortingOption;
                    currentPageNumber = pageNumber;
                    response.getWriter().write(previousPageResults.toString());
                }
            }
            if (response.getStatus() == 500) { }
            else { response.setStatus(200); }
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            JsonArray errorArray = new JsonArray();
            jsonObject.addProperty("errorMessage", e.getMessage());
            errorArray.add(jsonObject);
            response.getWriter().write(errorArray.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
}
