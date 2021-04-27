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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "ModifyCartServlet", urlPatterns = "/api/modify-cart")
public class DecreaseCartServlet extends HttpServlet {
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8"); // Response mime type

        String command = request.getParameter("command");
        String title = request.getParameter("title");
        HttpSession session = request.getSession();

        HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");
        if (command.equals("increase")) {
            previousItems.put(title, previousItems.get(title) + 1);

        }
        else if (command.equals("decrease")){

            previousItems.put(title, previousItems.get(title) - 1);
        }
        else if (command.equals("delete"))
        {
            previousItems.remove(title);
        }

        session.setAttribute("previousItems", previousItems);

//        JsonObject responseJsonObject = new JsonObject();
//
//        JsonArray previousItemsJsonArray = new JsonArray();
//        for (Map.Entry<String, Integer> entry : previousItems.entrySet()) {
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("title", entry.getKey());
//            jsonObject.addProperty("quantity", entry.getValue());
//            previousItemsJsonArray.add(jsonObject);
//        }
//        responseJsonObject.add("previousItems", previousItemsJsonArray);
//
//        response.getWriter().write(responseJsonObject.toString());
    }
}
