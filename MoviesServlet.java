import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
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


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/stars"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedbexample")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement
            Statement statement = dbcon.createStatement();

            //First query
            String query = "select id, title, year, director, rating \n" +
                    "from ratings, movies\n" +
                    "where ratings.movieId = movies.id\n" +
                    "order by rating\n" +
                    "desc limit 20;";


            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
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

            // For loop that iterates through the already created jsonArray and modifies each
            // JsonObject to include the three stars
            for (int i = 0; i<jsonArray.size(); i++)
            {
                JsonObject movie = jsonArray.get(i).getAsJsonObject(); //convert from jsonElement to jsonObject
                String query2 = "select name\n" +
                        "from stars_in_movies, stars\n" +
                        "where movieId=" + movie.get("movie_id") + " and stars.id = stars_in_movies.starId\n" +
                        "limit 3"; //query 2

                System.out.println(query2);
                ResultSet rstemp = statement.executeQuery(query2); //Another resultset to execute 2nd query
                String stars = "";
                while (rstemp.next())
                {
                    stars += rstemp.getString("name") + ", "; //append each star_id to empty string
                }
                stars = stars.replaceAll(", $", "");
                movie.addProperty("movie_stars", stars); //add the star_ids as a property of the jsonObject
                rstemp.close();
            }

            // For loop that iterates through the already created jsonArray and modifies each
            // JsonObject to include the three genres
           for (int i = 0; i<jsonArray.size(); i++)
           {
                JsonObject movie = jsonArray.get(i).getAsJsonObject();
                String query3 = "select name\n" +
                        "from genres_in_movies, genres\n" +
                        "where movieId=" + movie.get("movie_id") + " and genres.id = genres_in_movies.genreId\n" +
                        "limit 3";

                ResultSet rstemp = statement.executeQuery(query3);
                String genres = "";
                while (rstemp.next())
                {
                    genres += rstemp.getString("name") + ", ";
                }
                genres = genres.replaceAll(", $", "");
                movie.addProperty("movie_genres", genres);
                rstemp.close();
           }
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

        }
        out.close();

    }
}
