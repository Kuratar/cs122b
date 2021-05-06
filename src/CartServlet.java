import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
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
        response.setContentType("application/json; charset=utf-8"); // Response mime type

        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, Integer>();
        }
        JsonArray previousItemsJsonArray = new JsonArray();
        for (Map.Entry<String,Integer> entry: previousItems.entrySet())
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("title", entry.getKey());
            jsonObject.addProperty("quantity", entry.getValue());
            previousItemsJsonArray.add(jsonObject);
        }
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8"); // Response mime type

        String id = request.getParameter("id");
        String title = "";
        // query to check if user exists within database
        String userQuery = "select title\n" +
                "from movies\n" +
                "where id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(userQuery)) {

            statement.setString(1, id);
            // Perform check user query
            ResultSet rs = statement.executeQuery(userQuery);

            if (rs.next()) {
                title = rs.getString("title");
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
        HttpSession session = request.getSession();

        HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, Integer>();
            previousItems.put(title,1);
            session.setAttribute("previousItems", previousItems);

        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                if (previousItems.containsKey(title))
                {
                    previousItems.put(title, previousItems.get(title)+1);
                }
                else
                {
                    previousItems.put(title, 1);
                }
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousItemsJsonArray = new JsonArray();
        for (Map.Entry<String,Integer> entry: previousItems.entrySet())
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("title", entry.getKey());
            jsonObject.addProperty("quantity", entry.getValue());
            previousItemsJsonArray.add(jsonObject);
        }
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }
}

