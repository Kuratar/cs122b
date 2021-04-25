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
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
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

    // Use http GET
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        //response.setContentType("application/json");    // Response mime type

        PrintWriter out = response.getWriter();
        String title = request.getParameter("title");
        System.out.println(title);
        String year = request.getParameter("year");
        System.out.println(year);
        String director = request.getParameter("director");
        System.out.println(director);
        String star = request.getParameter("star");
        System.out.println(star);


        try {

            // Create a new connection to database
            Connection dbCon = dataSource.getConnection();

            // Declare a new statement
            Statement statement = dbCon.createStatement();

            // Retrieve parameter "name" from the http request, which refers to the value of <input name="name"> in index.html


            // Generate a SQL query
            String query = "SELECT movies.id, title, year, director, rating from ratings, movies";

            if (!star.isEmpty())

            {
                query += ", stars_in_movies, stars where stars.name like '%" + star + "%' and stars_in_movies.movieId = movies.id and stars.id = stars_in_movies.starId";
            }

            if (!title.isEmpty())
            {
                if (star.isEmpty())
                {
                    query += " where ";
                }
                else
                {
                    query += " and ";
                }
                 query+= "title like '%" + title + "%'";
            }
            if (!year.isEmpty())
            {
                if(title.isEmpty() && star.isEmpty())
                {
                    query += " where ";
                }
                else
                {
                    query += " and ";
                }
                query += "year = " + year;
            }
            if (!director.isEmpty())
            {
                if(title.isEmpty() && year.isEmpty() && star.isEmpty())
                {
                    query += " where ";
                }
                else
                {
                    query += " and ";
                }
                query += "director like '%" + director + "%'";
            }
            query += " and ratings.movieId = movies.id";
            System.out.println(query);

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs and create a table row <tr>
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);


                jsonArray.add(jsonObject);
            }
            for (int i = 0; i<jsonArray.size(); i++)
            {
                JsonObject movie = jsonArray.get(i).getAsJsonObject(); //convert from jsonElement to jsonObject
                String query2 = "select stars.id, name\n" +
                        "from stars_in_movies, stars\n" +
                        "where movieId=" + movie.get("movie_id") + " and stars.id = stars_in_movies.starId\n" +
                        "limit 3"; //query 2

                ResultSet rstemp = statement.executeQuery(query2); //Another resultset to execute 2nd query
                String stars = "";
                String starIds = "";
                while (rstemp.next())
                {
                    stars += rstemp.getString("name") + ", "; //append each star_id to empty string
                    starIds += rstemp.getString("id") + ", ";
                }
                starIds = starIds.substring(0, starIds.length()-2);
                stars = stars.substring(0, stars.length()-2);
                movie.addProperty("star_ids", starIds);
                movie.addProperty("star_names", stars); //add the star_ids as a property of the jsonObject
                rstemp.close();
            }

            // For loop that iterates through the already created jsonArray and modifies each
            // JsonObject to include the three genres
            for (int i = 0; i<jsonArray.size(); i++)
            {
                JsonObject movie = jsonArray.get(i).getAsJsonObject();
                String query3 = "select genres.id, name\n" +
                        "from genres_in_movies, genres\n" +
                        "where movieId=" + movie.get("movie_id") + " and genres.id = genres_in_movies.genreId\n" +
                        "limit 3";

                ResultSet rstemp = statement.executeQuery(query3);
                String genres = "";
                String genreIDs = "";
                while (rstemp.next())
                {
                    genres += rstemp.getString("name") + ", ";
                    genreIDs += rstemp.getString("id") + ", ";
                }
                genreIDs = genreIDs.substring(0, genreIDs.length()-2);
                genres = genres.substring(0, genres.length()-2);
                movie.addProperty("genre_names", genres);
                movie.addProperty("genre_ids", genreIDs);
                rstemp.close();
            }


            rs.close();
            statement.close();

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);


        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
        // always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
