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
        final ArrayList<Movie> movies = new ArrayList<>();
        movies.add(new Movie("tt0362227","The Terminal", (short) 2004, "Steven Spielberg", "Comedy, Drama, Romance"));
        movies.add(new Movie("tt0449018", "The Final Season", (short) 2007, "David Mickey Evans", "Drama, Sport"));

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
            intent.putExtra(year, movie.getYear());
            intent.putExtra(director, movie.getDirector());
            intent.putExtra(genres, movie.getGenres());
            startActivity(intent);

//            final RequestQueue queue = NetworkManager.sharedManager(this).queue;
//            // request type is POST
//            final StringRequest loginRequest = new StringRequest(
//                    Request.Method.GET,
//                    baseURL + "/api/single-movie?id=",
//                    response -> {
//                        // TODO: should parse the json response to redirect to appropriate functions
//                        //  upon different response value.
//                        Log.d("login.success", response);
//                        // initialize the activity(page)/destination
//                        Intent listPage = new Intent(this, ListViewActivity.class);
//                        // activate the list page.
//                        startActivity(listPage);
//                    },
//                    error -> {
//                        // error
//                        Log.d("login.error", error.toString());
//                    }) {
//                @Override
//                protected Map<String, String> getParams() {
//                    // POST request form data
//                    final Map<String, String> params = new HashMap<>();
//                    params.put("username", username.getText().toString());
//                    params.put("password", password.getText().toString());
//
//                    return params;
//                }
//            };

            // important: queue.add is where the login request is actually sent
//            queue.add(loginRequest);
        });
    }
}