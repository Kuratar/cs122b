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
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String mobile = request.getParameter("mobile");

//        if (gRecaptchaResponse != null && mobile == null) {
//            try {
//                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//            } catch (Exception e) {
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("recaptcha", "fail");
//                jsonObject.addProperty("recaptcha-message", "Recaptcha verification error, please try again.");
//                response.getWriter().write(jsonObject.toString());
//
//                return;
//            }
//        }

        // query to check if user exists within database
        String userQuery = "select *\n" +
                "from customers\n" +
                "where email=?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(userQuery)) {
            JsonObject responseJsonObject = new JsonObject();

            statement.setString(1, email);

            // Perform check user query
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                if (new StrongPasswordEncryptor().checkPassword(password, encryptedPassword)) {
                    int userId = rs.getInt("id");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    int ccId = rs.getInt("ccId");
                    String address = rs.getString("address");

                    request.getSession().setAttribute("user", new User(userId, firstName, lastName, ccId, address));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
                else {
                    // wrong password
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Incorrect email or password");
                }
            } else {
                // wrong email
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Incorrect email or password");
            }

            rs.close();
            statement.close();
            response.setStatus(200);
            response.getWriter().write(responseJsonObject.toString());
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
