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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "InsertMovieServlet", urlPatterns = "/api/insert-movie")
public class InsertMovieServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String releaseYear = request.getParameter("releaseYear");
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String genreName = request.getParameter("genreName");
        String starExists = request.getParameter("starExists");
        String genreExists = request.getParameter("genreExists");

        String query = "select max(movies.id), max(stars.id), max(genres.id) from movies, stars, genres";
        String query2 = "insert into movies (id, title, year, director) values (?,?,?,?);";
        String query3 = "insert into stars (id, name, birthYear) values (?,?,null);";
        String query4 = "insert into genres (id, name) values (?,?);";
        String query5 = "insert into stars_in_movies (starId, movieId) values (?,?);";
        String query6 = "insert into genres_in_movies (genreId, movieId) values (?,?);";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             PreparedStatement statement2 = conn.prepareStatement(query2);
             PreparedStatement statement3 = conn.prepareStatement(query3);
             PreparedStatement statement4 = conn.prepareStatement(query4);
             PreparedStatement statement5 = conn.prepareStatement(query5);
             PreparedStatement statement6 = conn.prepareStatement(query6)) {
            conn.setAutoCommit(false);
            JsonObject jsonObject = new JsonObject();

            ResultSet rs = statement.executeQuery();    rs.next();
            String movieId = rs.getString("max(movies.id)");
            String starId = rs.getString("max(stars.id)");
            int movieInt = Integer.parseInt(movieId.replaceAll("[^0-9]", ""))+1;
            String movieChars = movieId.replaceAll("[^a-zA-Z]", "");
            int starInt = Integer.parseInt(starId.replaceAll("[^0-9]", ""))+1;
            String starChars = starId.replaceAll("[^a-zA-Z]", "");

            int genreId = rs.getInt("max(genres.id)")+1;

            statement2.setString(1, movieChars+movieInt);
            statement2.setString(2, title);
            statement2.setInt(3, Integer.parseInt(releaseYear));
            statement2.setString(4, director);
            statement2.executeUpdate();

            if (starExists.equals("-1")) {
                statement3.setString(1, starChars+starInt);
                statement3.setString(2, starName);
                statement5.setString(1,starChars+starInt);
                statement3.executeUpdate();
                jsonObject.addProperty("starId", starChars+starInt);
                jsonObject.addProperty("starExists", "0");
            }
            else {
                statement5.setString(1, starExists);
                jsonObject.addProperty("starId", starExists);
                jsonObject.addProperty("starExists", "1");
            }
            statement5.setString(2, movieChars+movieInt);
            statement5.executeUpdate();

            if (genreExists.equals("-1")) {
                statement4.setInt(1, genreId);
                statement4.setString(2, genreName);
                statement6.setInt(1, genreId);
                statement4.executeUpdate();
                jsonObject.addProperty("genreId", genreId);
                jsonObject.addProperty("genreExists", "0");
            }
            else {
                statement6.setInt(1, Integer.parseInt(genreExists));
                jsonObject.addProperty("genreId", genreExists);
                jsonObject.addProperty("genreExists", "1");
            }
            statement6.setString(2, movieChars+movieInt);
            statement6.executeUpdate();

            conn.commit();

            jsonObject.addProperty("status", "success");
            jsonObject.addProperty("message", "Successfully added new movie");
            jsonObject.addProperty("movieId", movieChars+movieInt);

            rs.close();
            statement.close();
            statement2.close();
            statement3.close();
            statement4.close();
            statement5.close();
            statement6.close();
            response.setStatus(200);
            response.getWriter().write(jsonObject.toString());
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
