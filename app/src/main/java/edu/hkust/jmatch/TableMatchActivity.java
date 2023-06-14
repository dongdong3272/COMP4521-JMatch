package edu.hkust.jmatch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.hkust.jmatch.NLP.TextMatching;
import edu.hkust.jmatch.backend.Global;
import edu.hkust.jmatch.backend.PostRequest;

public class TableMatchActivity extends AppCompatActivity {

    private String textToBeMatched;
    private List<Map.Entry<String, Double>> unorderedCompanies;
    private TableLayout companyTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_match);

        // Initialize variables
        companyTable = findViewById(R.id.companyTable);
        Intent intent = getIntent();
        textToBeMatched = intent.getStringExtra("textToBeMatched");

        // Initialize the displayed text
        TextView mNameText = findViewById(R.id.cNameText);
        mNameText.setText(Global.User.isCandidate ? "Company Name" : "Candidate Name");
        TextView mTitleText = findViewById(R.id.titleText);
        String title = "Here are the statistics of your matched " + (Global.User.isCandidate ? "companies" : "candidates") + ":";
        mTitleText.setText(title);

        // Set the Backward button on Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the JDs for candidate / Get the CLs for companies
        getComparisonText();
    }

    private void getComparisonText() {
        String requestBaseURL = Global.BaseURL.queryBaseURL;
        String dbCollectionName = Global.User.isCandidate ? "jds" : "cvs";
        String queryString = String.format("db.collection(\"%s\")" +
                        ".orderBy(\"time\", \"desc\")" +
                        ".limit(100)" +
                        ".get()"
                , dbCollectionName);
        Log.d("Find JDs/CLs Query", queryString);

        new PostRequest(
                new PostRequest.ProcessRequestResult() {
                    @Override
                    public void processResult(JSONObject resultJSON) throws JSONException {
                        JSONArray dataArray = resultJSON.getJSONArray("data");
                        Map<String, Double> scoreMap = new HashMap<>();

                        // Populate scoreMap with data
                        for (int i = 0; i < dataArray.length(); i++) {
                            String documentString = dataArray.getString(i);
                            JSONObject documentJSON = new JSONObject(documentString);
                            String content = documentJSON.getString("content");
                            String username = documentJSON.getString("username");
                            double matchScore = -1;
                            try{
                                matchScore = TextMatching.computeDistance(textToBeMatched, content);
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                            scoreMap.put(username, matchScore);
                        }
                        // Initialize and order the unorderedCompanies
                        unorderedCompanies = new ArrayList<>(scoreMap.entrySet());
                        unorderedCompanies.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
                        // Only keep 10 keywords with highest score
                        unorderedCompanies = unorderedCompanies.subList(0, Math.min(unorderedCompanies.size(), 10));
                        // Populate the table
                        int rank = 1;
                        for (Map.Entry<String, Double> entry : unorderedCompanies) {
                            TableRow singleRow = getOneRow(rank++,entry.getKey(), entry.getValue());
                            companyTable.addView(singleRow);
                        }
                    }
                }
        ).execute(queryString, requestBaseURL);
    }

    public void goToNextPage2(View v) {
        Toast.makeText(Global.context, "Already Last Page", Toast.LENGTH_SHORT).show();
    }

    public void goToPrevPage2(View v) {
        Intent prevPageIntent = new Intent(TableMatchActivity.this, KeywordMatchActivity.class);
        prevPageIntent.putExtra("textToBeMatched", textToBeMatched);
        startActivity(prevPageIntent);
    }

    public TableRow getOneRow(int rank, String companyName, double matchScore) {
        TableRow row = new TableRow(this);
        // Create the TextViews
        TextView mRank = new TextView(this); TextView mCompanyName = new TextView(this); TextView mMatchScore = new TextView(this);
        // Set the parameters
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 70f);
        TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 200f);
        TableRow.LayoutParams params3 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 100f);
        mRank.setLayoutParams(params1); mCompanyName.setLayoutParams(params2); mMatchScore.setLayoutParams(params3);
        // Set style
        addStyleOnTextView(mRank); addStyleOnTextView(mCompanyName); addStyleOnTextView(mMatchScore);
        // Set content
        mRank.setText(Integer.toString(rank)); mCompanyName.setText(companyName); mMatchScore.setText(String.format("%.2f", matchScore));
        // Add to the row
        row.addView(mRank);row.addView(mCompanyName);row.addView(mMatchScore);
        // Set style of the row
        row.setBackgroundResource(R.drawable.row_border);
        return row;
    }

    public void addStyleOnTextView(TextView v) {
        v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);        // Font Type
        v.setTypeface(Typeface.DEFAULT_BOLD);                       // Bold
        v.setGravity(Gravity.CENTER);                               // Center
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
            Intent goToLogInIntent = new Intent(TableMatchActivity.this, LoginActivity.class);
            startActivity(goToLogInIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}