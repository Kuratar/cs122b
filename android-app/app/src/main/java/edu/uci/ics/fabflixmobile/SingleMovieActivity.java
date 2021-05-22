package edu.uci.ics.fabflixmobile;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.Map;

public class SingleMovieActivity extends Activity {
    private final String host = "10.0.2.2";
    private final String port = "8080";
    private final String domain = "cs122b_spring21_project1_api_example_war";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    public String title;
    public String year;
    public String director;
    public String genres;
    public String stars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemovie);

        // Get the Intent that started this activity and extract the string

        Intent intent = getIntent();
        String id = intent.getStringExtra(ListViewActivity.id);
        String title = intent.getStringExtra(ListViewActivity.title);
        int year = intent.getIntExtra(ListViewActivity.year, 0);
        String director = intent.getStringExtra(ListViewActivity.director);
        String genres = intent.getStringExtra(ListViewActivity.genres);

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest singleMovieRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + id + "&list=search&title=" + title + "&year=&director=&star=&nMovies=10&page=0&sorting=default",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    Log.d("login.success", response);
                    // initialize the activity(page)/destination
                    // activate the list page.
                    handleResults(response);
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                });
//            @Override
//            protected Map<String, String> getParams() {
//                // POST request form data
//                final Map<String, String> params = new HashMap<>();
//                params.put("username", username.getText().toString());
//                params.put("password", password.getText().toString());
//
//                return params;
//            }
//        };

        // important: queue.add is where the login request is actually sent
        queue.add(singleMovieRequest);
        // Capture the layout's TextView and set the string as its text


    }

    private void handleResults(String response){
        try
        {
            System.out.println("This is the json array");
            System.out.println(response);
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonObj = jsonArray.getJSONObject(0);
            title = "Movie Title: " + jsonObj.getString("movie_title") + "\n\n";
            year = "Release Year: " + jsonObj.getInt("movie_year") + "\n\n";
            director = "Director: " + jsonObj.getString("movie_director") + "\n\n";
            genres = "Genre(s): " + jsonObj.getString("movie_genreNames") + "\n\n";
            stars = "Stars: " + jsonObj.getString("movie_starNames") + "\n\n";
            TextView textView2 = findViewById(R.id.textView2);
            textView2.setText(title);
            textView2.append(String.valueOf(year));
            textView2.append(director);
            textView2.append(genres);
            textView2.append(stars);

            System.out.println(jsonObj.toString());
        }
        catch (JSONException err)
        {
            err.printStackTrace();
        }


    }
}