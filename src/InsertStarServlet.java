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

@WebServlet(name = "InsertStarServlet", urlPatterns = "/api/insert-star")
public class InsertStarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            //dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample-master");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String birthYear = request.getParameter("birth_year");

        String query = "select max(id) from stars";

        String query2 = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             PreparedStatement statement2 = conn.prepareStatement(query2)) {
            JsonObject jsonObject = new JsonObject();

            // get max id and rs.next to the row
            ResultSet rs = statement.executeQuery();    rs.next();
            // convert the digit part of id to int
            int maxId = Integer.parseInt(rs.getString("max(id)").substring(2));
            // set parameters for query2 and execute
            statement2.setString(1, "nm"+maxId+1);
            statement2.setString(2, name);
            if (birthYear.equals("")) { statement2.setString(3, null); }
            else { statement2.setInt(3, Integer.parseInt(birthYear)); }
            statement2.executeUpdate();

            jsonObject.addProperty("status", "success");
            jsonObject.addProperty("message", "Successfully added new star "+"nm"+(maxId+1));

            rs.close();
            statement.close();
            statement2.close();
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
