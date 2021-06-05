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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "AutoSearchServlet", urlPatterns = "/api/auto-search")
public class AutoSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String currentNMovies;
    private String currentPageNumber;
    private String currentSortingOption;
    // holds the next page of results
    private JsonArray nextPageResults;

    private FileWriter autoSearchTimes;
    private long queryElapsed;
    private long query2Elapsed;
    private long query3Elapsed;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private JsonArray generateResults(HttpServletResponse response, Connection conn,
                                      String keywords, String nMovies, String pageNumber, String sortingOption)
    {
        //-- select id, title from movies where match (title) against ('+s* +lov*' in boolean mode);
        String query = "SELECT movies.id, title, year, director, rating FROM movies, ratings WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE)" +
                " and ratings.movieId = movies.id\n";
        switch (sortingOption) {
            case "titleRatingASCE": query += "order by title, rating\n";        break;
            case "titleRatingDESC": query += "order by title desc, rating\n";   break;
            case "ratingTitleASCE": query += "order by rating, title\n";        break;
            case "ratingTitleDESC": query += "order by rating desc, title\n";   break;
        }
        query += "limit ?\n" +
                "offset ?";

        // get top 3 stars by most movies starred in
        String query2 = "select s.id, s.name, count(movieId) as movies\n" +
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

        // get top 3 genres
        String query3 = "select genres.id, name\n" +
                "from genres_in_movies, genres\n" +
                "where movieId=? and genres.id = genres_in_movies.genreId\n" +
                "order by name\n" +
                "limit 3";

        try (PreparedStatement statement = conn.prepareStatement(query);
             PreparedStatement statement2 = conn.prepareStatement(query2);
             PreparedStatement statement3 = conn.prepareStatement(query3)) {
            JsonArray jsonArray = new JsonArray();

            // make all keywords have + before and * after it and place into query

            statement.setString(1, keywords);
            statement.setInt(2, Integer.parseInt(nMovies));
            statement.setInt(3, Integer.parseInt(pageNumber) * Integer.parseInt(nMovies));

            long queryStart = System.nanoTime();
            ResultSet rs = statement.executeQuery();
            long queryEnd = System.nanoTime();
            queryElapsed = queryEnd - queryStart;

            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                // set the movieId for getting genre and star queries
                statement2.setString(1, movie_id);
                statement3.setString(1, movie_id);

                long query2Start = System.nanoTime();
                ResultSet rs2 = statement2.executeQuery();
                long query2End = System.nanoTime();
                query2Elapsed = query2End - query2Start;

                long query3Start = System.nanoTime();
                ResultSet rs3 = statement3.executeQuery();
                long query3End = System.nanoTime();
                query3Elapsed = query3End - query3Start;

                // get ids and names of genre and stars into a string
                String genreIds = "";
                String genreNames = "";
                String starIds = "";
                String starNames = "";
                for (int j = 0; j < 3; j++) {
                    if (rs3.next()) {
                        genreIds += rs3.getString("id") + ", ";
                        genreNames += rs3.getString("name") + ", ";
                    }
                    if (rs2.next()) {
                        starIds += rs2.getString("id") + ", ";
                        starNames += rs2.getString("name") + ", ";
                    }
                }
                // remove last comma and space
                if (!genreIds.isEmpty()) {
                    genreIds = genreIds.substring(0, genreIds.length()-2);
                    genreNames = genreNames.substring(0, genreNames.length()-2);
                }
                if (!starIds.isEmpty()) {
                    starIds = starIds.substring(0, starIds.length()-2);
                    starNames = starNames.substring(0, starNames.length()-2);
                }
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
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

    private JsonArray generateSuggestions(String keywords) {
        //select id, title from movies where match (title) against ('+lo*' in boolean mode);
        String query = "SELECT id, title FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);) {
            JsonArray jsonArray = new JsonArray();

            statement.setString(1, keywords);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("value", movieTitle);

                JsonObject jsonObject1 = new JsonObject();
                jsonObject1.addProperty("movieId", movieId);

                jsonObject.add("data", jsonObject1);
                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();
            return jsonArray;
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            JsonArray errorArray = new JsonArray();
            jsonObject.addProperty("errorMessage", e.getMessage() + " from generateSuggestions");
            errorArray.add(jsonObject);
            return errorArray;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // handle user inputs from autocomplete box
        // only reached if parameter in given link has "input" which is only written to link if the user has not
        // pressed enter or clicked the search button, otherwise it would be parameter "query" for completed user input
        try {
            autoSearchTimes = new FileWriter("/home/ubuntu/" +
                                                "cs122b-spring21-team-93/autoSearchPerformance/performances/single1.txt",true);

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            throw e;
        }
        long servletStart = System.nanoTime();

        String userInput = request.getParameter("input");
        if (userInput != null) {
            // create the string for full text matching in boolean mode
            String[] inputSplit = userInput.split(" ");
            String keywords = "";
            for (String s : inputSplit) {
                keywords += "+" + s + "* ";
            }
            keywords = keywords.substring(0, keywords.length()-1);

            // call the function to create the results
            JsonArray suggestions = generateSuggestions(keywords);

            // if the size of the results is 1 and the object has errorMessage, set response as failure
            if (suggestions.size() == 1 && suggestions.get(0).getAsJsonObject().get("errorMessage") != null) {
                response.setStatus(500);
            }
            else { response.setStatus(200); }

            response.getWriter().write(suggestions.toString());
            return;
        }
        else { response.setContentType("application/json; charset=utf-8"); }    // Response mime type

        String query = request.getParameter("query");
        String nMovies = request.getParameter("nMovies");
        String pageNumber = request.getParameter("page");
        String sortingOption = request.getParameter("sorting");

        String[] querySplit = query.split(" ");
        String keywords = "";
        for (String s : querySplit) {
            keywords += "+" + s + "* ";
        }
        keywords = keywords.substring(0, keywords.length()-1);

        try (Connection conn = dataSource.getConnection()) {
            // if the page is an even number, always generate the current page of results and the next
            if (Integer.parseInt(pageNumber) % 2 == 0) {
                JsonArray jsonArray = generateResults(response, conn, keywords, nMovies, pageNumber, sortingOption);
                response.getWriter().write(jsonArray.toString());
                JsonArray nextPageArray = generateResults(response, conn, keywords, nMovies, Integer.toString(Integer.parseInt(pageNumber)+1), sortingOption);
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
                        nextPageResults = generateResults(response, conn, keywords, nMovies, pageNumber, sortingOption);
                        currentNMovies = nMovies;
                        currentSortingOption = sortingOption;
                    }
                    // statement reached if settings are same thus send nextPage
                    currentPageNumber = pageNumber;
                    response.getWriter().write(nextPageResults.toString());
                }
                // if the page number is not the next page, generate results for previous page and send
                else {
                    JsonArray previousPageResults = generateResults(response, conn, keywords, nMovies, pageNumber, sortingOption);
                    currentNMovies = nMovies;
                    currentSortingOption = sortingOption;
                    currentPageNumber = pageNumber;
                    response.getWriter().write(previousPageResults.toString());
                }
            }
            if (response.getStatus() == 500) { }
            else { response.setStatus(200); }

            long servletEnd = System.nanoTime();
            long servletElapsed = servletEnd - servletStart;
            autoSearchTimes.write(queryElapsed + " " + query2Elapsed + " " + query3Elapsed + " " + servletElapsed + "\n");
            autoSearchTimes.close();
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            JsonArray errorArray = new JsonArray();
            jsonObject.addProperty("errorMessage", e.getMessage() + "from generateResults");
            errorArray.add(jsonObject);
            response.getWriter().write(errorArray.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
}
