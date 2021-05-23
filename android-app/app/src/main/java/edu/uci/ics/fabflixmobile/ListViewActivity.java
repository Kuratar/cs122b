package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private RequestQueue queue;

    private ListView movieList;
    private Button prevButton;
    private Button nextButton;
    private TextView pageNumber;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        movieList = findViewById(R.id.list);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        pageNumber = findViewById(R.id.pageNumber);

        // TODO: this should be retrieved from the backend server
        queue = NetworkManager.sharedManager(this).queue;
        Intent searchIntent = getIntent();
        query = searchIntent.getStringExtra("query");
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

        prevButton.setOnClickListener(view -> prev());
        nextButton.setOnClickListener(view -> next());
    }

    public void prev() {
        int number = Integer.parseInt(pageNumber.getText().toString().substring(5));
        if (number == 0) {
            Toast.makeText(getApplicationContext(),"Already on the first page",Toast.LENGTH_SHORT).show();
            return;
        }
        final StringRequest searchRequest = makeRequest(query, Integer.toString(number-1));
        queue.add(searchRequest);
        pageNumber.setText("Page " + (number-1));
    }

    public void next() {
        int number = Integer.parseInt(pageNumber.getText().toString().substring(5))+1;
        AtomicBoolean end = new AtomicBoolean(false);
        final ArrayList<Movie> movies = new ArrayList<>();
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/auto-search?query=" + query + "&nMovies=20&page=" + number + "&sorting=default",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    try{
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() == 0) {
                            Toast.makeText(getApplicationContext(), "No more movies", Toast.LENGTH_SHORT).show();
                        }
                        else {
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
                            pageNumber.setText("Page " + (number));
                        }
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
    }

    public StringRequest makeRequest(String query, String pageNumber) {
        final ArrayList<Movie> movies = new ArrayList<>();
        return new StringRequest(
            Request.Method.GET,
            baseURL + "/api/auto-search?query=" + query + "&nMovies=20&page=" + pageNumber + "&sorting=default",
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
    }
}