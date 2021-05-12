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
import java.sql.*;

@WebServlet(name = "CheckMovieProcedure", urlPatterns = "/api/check-movie")
public class CheckMovieProcedure extends HttpServlet {
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
        String releaseYear = request.getParameter("release_year");
        String director = request.getParameter("director");
        String starName = request.getParameter("star_name");
        String genreName = request.getParameter("genre");

        String query = "{call add_movie(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = dataSource.getConnection();
             CallableStatement statement = conn.prepareCall(query)) {
            JsonObject jsonObject = new JsonObject();

            statement.setString(1, title);
            statement.setInt(2, Integer.parseInt(releaseYear));
            statement.setString(3, director);
            statement.setString(4, starName);
            statement.setString(5, genreName);
            statement.registerOutParameter(6, Types.INTEGER);
            statement.registerOutParameter(7, Types.VARCHAR);
            statement.registerOutParameter(8, Types.INTEGER);

            statement.execute();

            int movieExists = statement.getInt("@movieExists");
            String starExists = statement.getString("@starExists");
            int genreExists = statement.getInt("@genreExists");
            jsonObject.addProperty("movieExists", movieExists);
            jsonObject.addProperty("starExists", starExists);
            jsonObject.addProperty("genreExists", genreExists);
            jsonObject.addProperty("title", title);
            jsonObject.addProperty("releaseYear", releaseYear);
            jsonObject.addProperty("director", director);
            jsonObject.addProperty("starName", starName);
            jsonObject.addProperty("genreName", genreName);

            statement.close();
            response.setStatus(200);
            response.getWriter().write(jsonObject.toString());
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
            response.getWriter().write(jsonObject.toString());
        }
    }
}
