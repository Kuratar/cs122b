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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/api/place-order")
public class PlaceOrderServlet extends HttpServlet {
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
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String first_name = request.getParameter("first-name");
        String last_name = request.getParameter("last-name");
        String card_number = request.getParameter("card-number");
        String expiry_date = request.getParameter("expiry-date");

        // query to check if user exists within database
        String userQuery = "select *\n" +
                "from creditcards\n" +
                "where id=? and expiration=?";
        String query2 = "select id\n" +
                "from customers\n" +
                "where firstName=? and lastName=?";
        String query3 = "select id\n" +
                "from movies\n" +
                "where title=?";
        String updateQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?)";

        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(userQuery);
             PreparedStatement statement2 = conn.prepareStatement(query2);
             PreparedStatement statement3 = conn.prepareStatement(query3);
             PreparedStatement statement4 = conn.prepareStatement(updateQuery)) {
            // place in id and expiration
            statement.setString(1, card_number);
            statement.setString(2, expiry_date);

            // Perform check user query
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

                // place in firstName and lastName
                statement2.setString(1, first_name);
                statement2.setString(2, last_name);

                ResultSet rs2 = statement2.executeQuery();
                int customerId = 0;
                if (rs2.next())
                {
                    customerId = rs2.getInt("id");
                }

                HttpSession session = request.getSession();
                Date date = new Date();
                String date_str = String.format("%tF", date);

                HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");
                int total = 0;
                String sale_info = "";
                for(Map.Entry<String,Integer> entry: previousItems.entrySet())
                {
                    String title = entry.getKey();
                    int quantity = entry.getValue();

                    sale_info += title + "\t" + quantity + "\t$" + quantity*10 + "\n";
                    total += quantity * 10;
                    String movieId = "";

                    // place in movie title
                    statement3.setString(1, title);

                    ResultSet rs3 = statement3.executeQuery();
                    if (rs3.next())
                    {
                        movieId = rs3.getString("id");
                    }

                    for (int i = 0; i<quantity; i++)
                    {
                        statement4.setInt(1, customerId);
                        statement4.setString(2, movieId);
                        statement4.setString(3, date_str);
                        statement4.executeUpdate();
                    }

                    rs3.close();
                }
                responseJsonObject.addProperty("total", total);
                responseJsonObject.addProperty("sale_info", sale_info);

                rs2.close();
                session.removeAttribute("previousItems");
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Incorrect payment information, please try again.");
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
        response.getWriter().write(responseJsonObject.toString());
    }
}