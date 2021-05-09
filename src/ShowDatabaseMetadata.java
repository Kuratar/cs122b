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
import java.sql.Statement;

@WebServlet(name = "ShowDatabaseMetadata", urlPatterns = "/api/metadata")
public class ShowDatabaseMetadata extends HttpServlet {
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
        String query = "show tables";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             Statement statement2 = conn.createStatement();) {
            // array of JsonObjects which are the tables
            JsonArray tables = new JsonArray();

            // get all tables in database moviedbexample
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String query2 = "describe ";
                // the table themselves
                JsonObject table = new JsonObject();
                // the attributes within the tables
                JsonArray attributes = new JsonArray();

                // get table name, set table name in query to get table attributes
                String tableName = rs.getString("Tables_in_moviedbexample");
                query2 += tableName;

                // get table attributes
                ResultSet rs2 = statement2.executeQuery(query2);
                while (rs2.next()) {
                    String field = rs2.getString("Field");
                    String type = rs2.getString("Type");

                    // the single attribute
                    JsonObject attribute = new JsonObject();
                    attribute.addProperty("field", field);
                    attribute.addProperty("type", type);
                    attributes.add(attribute);
                }

                // create the table object with its name and attributes
                table.addProperty("name", tableName);
                table.add("attributes", attributes);
                // add the table object to the tables array
                tables.add(table);
                rs2.close();
            }

            rs.close();
            statement.close();
            statement2.close();
            response.setStatus(200);
            response.getWriter().write(tables.toString());
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            JsonArray errorArray = new JsonArray();
            jsonObject.addProperty("errorMessage", e.getMessage() + "from generateResults");
            errorArray.add(jsonObject);

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
            response.getWriter().write(errorArray.toString());
        }
    }
}
