package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListViewActivity extends Activity {
    private final String host = "10.0.2.2";
    private final String port = "8080";
    private final String domain = "cs122b_spring21_project1_api_example_war";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;
    
    public static final String title = "title";
    public static final String year = "year";
    public static final String director = "director";
    public static final String genres = "genres";
    public static final String id = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        // TODO: this should be retrieved from the backend server
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        Intent searchIntent = getIntent();
        String query = searchIntent.getStringExtra("query");
        final ArrayList<Movie> movies = new ArrayList<>();
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/auto-search?query=" + query + "&nMovies=20&page=0&sorting=default",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    try{
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i< jsonArray.length(); i++)
                        {
                            JSONObject jsonObj = jsonArray.getJSONObject(i);
                            String id = jsonObj.getString("movie_id");
                            String title = jsonObj.getString("movie_title");
                            int year = jsonObj.getInt("movie_year");
                            String director = jsonObj.getString("movie_director");
                            String genres = jsonObj.getString("genre_names");
                            String stars = jsonObj.getString("star_names");
                            movies.add(new Movie(id, title, year, director, genres, stars));
                        }
                        Log.d("search.success", response);
                        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);

                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getTitle(), movie.getYear());
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, SingleMovieActivity.class);
                            intent.putExtra(this.id, movie.getId());
                            intent.putExtra(title, movie.getTitle());
                            //intent.putExtra(year, movie.getYear());
                            //intent.putExtra(director, movie.getDirector());
                            //intent.putExtra(genres, movie.getGenres());
                            startActivity(intent);

                        });
                    }
                    catch(JSONException e){
                        e.printStackTrace();
                    }

                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                });
        queue.add(searchRequest);

        //movies.add(new Movie("tt0362227","The Terminal", (short) 2004, "Steven Spielberg", "Comedy, Drama, Romance", ""));
        //movies.add(new Movie("tt0449018", "The Final Season", (short) 2007, "David Mickey Evans", "Drama, Sport", ""));

        System.out.println(movies.isEmpty());

    }
}