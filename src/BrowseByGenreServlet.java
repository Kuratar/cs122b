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

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String genreId = request.getParameter("id");
        String nMovies = request.getParameter("nMovies");
        String pageNumber = request.getParameter("page");

        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();

            // query to get all movies associated with genre
            String query = "select id, title, year, director, rating\n" +
                    "from movies, ratings, genres_in_movies\n" +
                    "where genres_in_movies.genreId=" + genreId + " and " +
                    "genres_in_movies.movieId=movies.id and movies.id=ratings.movieId\n" +
                    "limit " + nMovies + "\n" +
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
                for (int i = 0; i < 3; i++) {
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
            response.setStatus(200);
            response.getWriter().write(jsonArray.toString());
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
