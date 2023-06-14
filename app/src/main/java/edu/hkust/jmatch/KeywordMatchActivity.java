package edu.hkust.jmatch;

import static edu.hkust.jmatch.NLP.TextMatching.extractKeywords;

import androidx.appcompat.app.AppCompatActivity;

import edu.hkust.jmatch.backend.Global;
import edu.hkust.jmatch.backend.PostRequest;
import edu.hkust.jmatch.adapter.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.Map;

public class KeywordMatchActivity extends AppCompatActivity {

    ListView barChart;

    private String textToBeMatched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyword_match);
        barChart = findViewById(R.id.barChart);

        // Set the Backward button on Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // First try to get the Text from intent
        Intent intent = getIntent();
        textToBeMatched = intent.getStringExtra("textToBeMatched");
        if(textToBeMatched != null){
            // If we already get the text, populate the table directly
            populateBarChart();
        } else {
            // Otherwise, retrieve the document from database and update the table
            findDocument();
        }
    }

    private void findDocument() {
        String documentID = Global.Document.documentID;
        String requestBaseURL = Global.BaseURL.queryBaseURL;
        String dbCollectionName = Global.User.isCandidate ? "cvs" : "jds";
        String queryString = String.format("db.collection(\"%s\")" +
                        ".where({" +
                        "documentID: \"%s\"," +
                        "})" +
                        ".limit(1)" +
                        ".get()"
                , dbCollectionName, documentID);
        Log.d("Find Match Record Query", queryString);

        new PostRequest(
                new PostRequest.ProcessRequestResult() {
                    @Override
                    public void processResult(JSONObject resultJSON) throws JSONException {
                        boolean findDocumentSuccess = resultJSON.getJSONArray("data").length() == 1;
                        if (findDocumentSuccess) {
                            // Update local variable
                            String dataJSONString = resultJSON.getJSONArray("data").getString(0);
                            JSONObject dataJSON = new JSONObject(dataJSONString);
                            textToBeMatched = dataJSON.getString("content");
                            populateBarChart();
                        } else {
                            Toast.makeText(Global.context, "No Document Found", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        ).execute(queryString, requestBaseURL);
    }


    private void populateBarChart() {
        try {
            List<Map.Entry<String, Double>> keywords = extractKeywords(this, textToBeMatched);
            String texts[] = new String[keywords.size()];
            double scores[] = new double[keywords.size()];
            int iter = 0;
            for(Map.Entry<String, Double> entry : keywords){
                texts[iter] = entry.getKey();
                scores[iter] = entry.getValue();
                iter++;
            }
            BarCharAdapter barCharAdapter = new BarCharAdapter(this, texts, scores);
            barChart.setAdapter(barCharAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void goToNextPage(View v){
        Intent nextPageIntent = new Intent(KeywordMatchActivity.this, TableMatchActivity.class);
        nextPageIntent.putExtra("textToBeMatched", textToBeMatched);
        startActivity(nextPageIntent);
    }
    public void goToPrevPage(View v){
        Toast.makeText(Global.context, "Already First Page", Toast.LENGTH_SHORT).show();
    }

    // Create the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Handle logout action here
            Global.User.loggedIn = false;
            Global.User.username = "";
            Global.User.isCandidate = true;
            Intent goToLogInIntent = new Intent(KeywordMatchActivity.this, LoginActivity.class);
            startActivity(goToLogInIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}