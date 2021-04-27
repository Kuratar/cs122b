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

        System.out.println(first_name);
        System.out.println(last_name);
        System.out.println(card_number);
        System.out.println(expiry_date);
        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            // Declare our statement
            Statement statement = conn.createStatement();

            // query to check if user exists within database
            String userQuery = "select *\n" +
                    "from creditcards\n" +
                    "where id='" + card_number + "' and " +
                    "expiration='" + expiry_date + "'";

            System.out.println(userQuery);
            // Perform check user query
            ResultSet rs = statement.executeQuery(userQuery);

            if (rs.next()) {

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

                String query2 = "select id\n" +
                        "from customers\n" +
                        "where firstName='" + first_name + "' and " +
                        "lastName='" + last_name + "'";
                System.out.println(query2);

                ResultSet rs2 = statement.executeQuery(query2);

                int customerId = 0;
                if (rs2.next())
                {
                    customerId = rs2.getInt("id");
                }


                HttpSession session = request.getSession();
                Date date = new Date();
                String date_str = String.format("%tF", date);

                HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");

                for(Map.Entry<String,Integer> entry: previousItems.entrySet())
                {
                    String title = entry.getKey();
                    int quantity = entry.getValue();
                    String movieId = "";

                    String query3 = "select id\n" +
                            "from movies\n" +
                            "where title='" + title + "'";

                    ResultSet rs3 = statement.executeQuery(query3);
                    System.out.println(query3);
                    if (rs3.next())
                    {
                        movieId = rs3.getString("id");
                        System.out.println(movieId);
                    }

                    for (int i = 0; i<quantity; i++)
                    {
                        String updateQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (" + customerId + ", '" + movieId + "', '" + date_str + "')";
                        System.out.println(updateQuery);
                        statement.executeUpdate(updateQuery);

                    }

                    rs3.close();
                }

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