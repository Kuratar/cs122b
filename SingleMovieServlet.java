//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(
        name = "SingleMovieServlet",
        urlPatterns = {"/api/single-movie"}
)
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public SingleMovieServlet() {
    }

    public void init(ServletConfig config) {
        try {
            this.dataSource = (DataSource)(new InitialContext()).lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException var3) {
            var3.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String id = request.getParameter("id");
        PrintWriter out = response.getWriter();

        try {
            Connection conn = this.dataSource.getConnection();

            try {
                String query = "select title, year, director, rating\n" +
                        "from movies, ratings\n" +
                        "where movies.id='" + id + "' and " +
                        "ratings.movieId='" + id + "'";
                PreparedStatement statement = conn.prepareStatement(query);
                //statement.setString(1, id);
                ResultSet rs = statement.executeQuery();
                String query2 = "select name\n" +
                        "from genres_in_movies, genres\n" +
                        "where movieId='" + id + "' and " +
                        "genres.id = genres_in_movies.genreId";
                PreparedStatement statement2 = conn.prepareStatement(query2);
                ResultSet rs2 = statement2.executeQuery();
                String query3 = "select stars.id, name\n" +
                        "from stars_in_movies, stars\n" +
                        "where movieId='" + id + "' and " +
                        "stars.id = stars_in_movies.starId;";
                PreparedStatement statement3 = conn.prepareStatement(query3);
                ResultSet rs3 = statement3.executeQuery();
                JsonArray jsonArray = new JsonArray();

                while(true) {
                    if (!rs.next()) {
                        rs.close();
                        statement.close();
                        out.write(jsonArray.toString());
                        response.setStatus(200);
                        break;
                    }
                    String title = rs.getString("title");
                    String year = rs.getString("year");
                    String director = rs.getString("director");
                    String rating = rs.getString("rating");

                    // get all the genres from the query results
                    String genres = "";
                    while (true) {
                        if (!rs2.next()) {
                            // get rid of last comma and space
                            genres = genres.substring(0, genres.length()-2);
                            rs2.close();
                            statement2.close();
                            break;
                        }
                        genres += rs2.getString("name") + ", ";
                    }

                    // get all the stars from the query results
                    String starIds = "";
                    String starNames = "";
                    while (true) {
                        if (!rs3.next()) {
                            // get rid of last comma and space
                            starIds = starIds.substring(0, starIds.length()-2);
                            starNames = starNames.substring(0, starNames.length()-2);
                            rs3.close();
                            statement3.close();
                            break;
                        }
                        starIds += rs3.getString("id") + ", ";
                        starNames += rs3.getString("name") + ", ";
                    }

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_title", title);
                    jsonObject.addProperty("movie_year", year);
                    jsonObject.addProperty("movie_director", director);
                    jsonObject.addProperty("movie_rating", rating);
                    jsonObject.addProperty("movie_genres", genres);
                    jsonObject.addProperty("movie_starIds", starIds);
                    jsonObject.addProperty("movie_starNames", starNames);
                    jsonArray.add(jsonObject);
                }
            } catch (Throwable var24) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var23) {
                        var24.addSuppressed(var23);
                    }
                }

                throw var24;
            }

            if (conn != null) {
                conn.close();
            }
        } catch (Exception var25) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", var25.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }

    }
}ASDASDASD