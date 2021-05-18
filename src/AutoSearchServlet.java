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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "AutoSearchServlet", urlPatterns = "/api/auto-search")
public class AutoSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");    // Response mime type

        String userInput = request.getParameter("query");
        // TODO: basically follow format of SearchServlet, need to create other queries, get genres/stars, etc
        String[] inputSplit = userInput.split(" ");
        //-- select id, title from movies where match (title) against ('+s* +lov*' in boolean mode);
        String query = "SELECT movies.id, title, year, director, rating FROM movies, ratings WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE)" +
                        " and ratings.movieId = movies.id\n";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            JsonArray jsonArray = new JsonArray();

            // make all keywords have + before and * after it and place into query
            String keywords = "";
            for (String s : inputSplit) {
                keywords += "+" + s + "* ";
            }
            keywords = keywords.substring(0, keywords.length()-1);
            statement.setString(1, keywords);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", id);
                jsonObject.addProperty("title", title);

                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();
            response.setStatus(200);
            response.getWriter().write(jsonArray.toString());
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
