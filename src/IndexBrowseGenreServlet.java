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
import java.sql.Statement;

@WebServlet(name = "IndexBrowseGenreServlet", urlPatterns = "/api/genres")
public class IndexBrowseGenreServlet extends HttpServlet {
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
        response.setContentType("application/json; charset=utf-8");

        JsonArray jsonArray = new JsonArray();

        try (Connection conn = dataSource.getConnection()) {
            // Declare our statement
            Statement statement = conn.createStatement();

            // query to get all genres
            String genreQuery = "select *\n" +
                                "from genres\n" +
                                "order by name;";

            // Perform get all genres query
            ResultSet rs = statement.executeQuery(genreQuery);

            while (rs.next()) {
                String genreId = rs.getString("id");
                String genreName = rs.getString("name");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genre_id", genreId);
                jsonObject.addProperty("genre_name", genreName);

                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();
            response.setStatus(200);
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }

        response.getWriter().write(jsonArray.toString());
    }
}
