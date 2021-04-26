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
import javax.xml.transform.Result;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

@WebServlet(name = "BrowseByGenreServlet", urlPatterns = "/api/browse-genre")
public class BrowseByGenreServlet extends HttpServlet {
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
                                      String genreId, String nMovies, String pageNumber, String sortingOption)
    throws IOException {
        try {
            JsonArray jsonArray = new JsonArray();

            // query to get all movies associated with genre
            String query = "select id, title, year, director, rating\n" +
                    "from movies, ratings, genres_in_movies\n" +
                    "where genres_in_movies.genreId=" + genreId + " and " +
                    "genres_in_movies.movieId=movies.id and movies.id=ratings.movieId\n";
            // sorting option if wanted - if none of these selected, return rows as is from database
            switch (sortingOption) {
                case "titleRatingASCE": query += "order by title, rating\n";            break;
                case "titleRatingDESC": query += "order by title desc, rating desc\n";  break;
                case "ratingTitleASCE": query += "order by rating, title\n";            break;
                case "ratingTitleDESC": query += "order by rating desc, title desc\n";  break;
            }
            query += "limit " + nMovies + "\n" +
                    // add i to current page number
                    // the first time, i will be 0 and thus will not change the current page number since even
                    // pages generate the current page of results and the next
                    // second time i will be 1 - this makes this query generate the next page of results
                    "offset " + Integer.parseInt(pageNumber) * Integer.parseInt(nMovies);
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                // get properties for movie
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating");

                // query to get first 3 genres based on alphabetical order
                String query2 = "select genres.id, name\n" +
                        "from genres_in_movies, genres\n" +
                        "where movieId='" + movieId + "' and genres.id = genres_in_movies.genreId\n" +
                        "order by name\n" +
                        "limit 3";
                PreparedStatement statement2 = conn.prepareStatement(query2);
                ResultSet rs2 = statement2.executeQuery();
                // query to get first 3 stars based on number of movies starred
                String query3 = "select s.id, s.name, count(movieId) as movies\n" +
                        "from (\n" +
                        "select stars.id, name\n" +
                        "from stars_in_movies, stars\n" +
                        "where movieId='" + movieId + "' and stars.id = stars_in_movies.starId\n" +
                        "order by id\n" +
                        ") as s, stars_in_movies\n" +
                        "where s.id = stars_in_movies.starId\n" +
                        "group by s.id\n" +
                        "order by movies desc\n" +
                        "limit 3";
                PreparedStatement statement3 = conn.prepareStatement(query3);
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
                statement2.close();
                statement3.close();
            }

            rs.close();
            statement.close();
            return jsonArray;
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage() + "from generateResults");
            response.getWriter().write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        JsonArray emptyArray = new JsonArray();
        return emptyArray;
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");

        String genreId = request.getParameter("id");
        String nMovies = request.getParameter("nMovies");
        String pageNumber = request.getParameter("page");
        String sortingOption = request.getParameter("sorting");

        try (Connection conn = dataSource.getConnection()) {
            // if the page is an even number, always generate the current page of results and the next
            if (Integer.parseInt(pageNumber) % 2 == 0) {
                JsonArray jsonArray = generateResults(response, conn, genreId, nMovies, pageNumber, sortingOption);
                response.getWriter().write(jsonArray.toString());
                JsonArray nextPageArray = generateResults(response, conn, genreId, nMovies, Integer.toString(Integer.parseInt(pageNumber)+1), sortingOption);
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
                        nextPageResults = generateResults(response, conn, genreId, nMovies, pageNumber, sortingOption);
                        currentNMovies = nMovies;
                        currentSortingOption = sortingOption;
                    }
                    // statement reached if settings are same thus send nextPage
                    currentPageNumber = pageNumber;
                    response.getWriter().write(nextPageResults.toString());
                }
                // if the page number is not the next page, generate results for previous page and send
                else {
                    JsonArray previousPageResults = generateResults(response, conn, genreId, nMovies, pageNumber, sortingOption);
                    currentNMovies = nMovies;
                    currentSortingOption = sortingOption;
                    currentPageNumber = pageNumber;
                    response.getWriter().write(previousPageResults.toString());
                }
            }
            response.setStatus(200);

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
}
