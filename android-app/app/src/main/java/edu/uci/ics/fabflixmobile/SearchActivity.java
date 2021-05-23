package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends Activity {
    private EditText search;
    private Button searchButton;

    private final String host = "10.0.2.2";
    private final String port = "8080";
    private final String domain = "cs122b_spring21_project1_api_example_war";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    public static final String query = "query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // upon creation, inflate and initialize the layout
        setContentView(R.layout.search);
        search = findViewById(R.id.search);
        searchButton = findViewById(R.id.searchbutton);

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> search());

        search.setOnKeyListener((v, keyCode, event) -> {
            //System.out.println(event.getAction());
            System.out.println(keyCode);
            if (event.getAction() == 0 && keyCode == KeyEvent.KEYCODE_ENTER) {
                search();
                return true;
            }
            return false;
        });
    }
    public void search() {
        //Send query over to ListViewActivity
        Intent searchResults = new Intent(this, ListViewActivity.class);
        searchResults.putExtra(query, search.getText().toString());
        startActivity(searchResults);

    }
}
