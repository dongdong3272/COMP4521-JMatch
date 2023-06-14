package edu.hkust.jmatch;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import edu.hkust.jmatch.adapter.MatchRecord;
import edu.hkust.jmatch.adapter.MatchRecordAdapter;
import edu.hkust.jmatch.backend.Global;
import edu.hkust.jmatch.backend.PostRequest;

public class MainActivity extends AppCompatActivity {

    private ArrayList<MatchRecord> matchRecordData;
    private RecyclerView matchRecordShelf;

    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText(getWelcomeText() + Global.User.username + "!");
        matchRecordData = new ArrayList<>();
        matchRecordShelf = findViewById(R.id.matchRecordShelf);
        matchRecordShelf.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        matchRecordShelf.addItemDecoration(new VerticalSpaceItemDecoration(40));
        MatchRecordAdapter emptyAdapter = new MatchRecordAdapter(MainActivity.this, matchRecordData);
        matchRecordShelf.setAdapter(emptyAdapter);

        // Populate the matchRecordShelf with users' match records
        populateWithRecords();
    }

    private void populateWithRecords() {
        // Step 1:  Query the database to get the match records by this user
        String requestBaseURL = Global.BaseURL.queryBaseURL;
        String dbCollectionName = Global.User.isCandidate ? "cvs" : "jds";
        String queryString = String.format("" +
                        "db.collection(\"%s\")" +
                        ".where({" +
                        "username: \"%s\"," +
                        "})" +
                        ".orderBy(\"time\", \"desc\")" +
                        ".limit(20)" +
                        ".get()"
                , dbCollectionName, Global.User.username);
        Log.d("Main Activity Query", queryString);
        new PostRequest(
                new PostRequest.ProcessRequestResult() {
                    @Override
                    public void processResult(JSONObject resultJSON) throws JSONException {
                        JSONArray dataArray = resultJSON.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            String matchRecordString = dataArray.getString(i);
                            JSONObject matchRecordJSON = new JSONObject(matchRecordString);
                            String documentID = matchRecordJSON.getString("documentID");
                            String title = matchRecordJSON.getString("title");
                            String time = matchRecordJSON.getString("time");
                            matchRecordData.add(new MatchRecord(documentID, title, time));
                        }
                        MatchRecordAdapter myAdapter = new MatchRecordAdapter(MainActivity.this, matchRecordData);
                        matchRecordShelf.setAdapter(myAdapter);
                    }
                }
        ).execute(queryString, requestBaseURL);
    }

    public void startMatching(View v) {
        System.out.println("Start Matching");
        Intent submitMatchIntent = new Intent(this, SubmitMatchActivity.class);
        startActivity(submitMatchIntent);
    }

    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
            outRect.top = verticalSpaceHeight;
        }
    }

    private String getWelcomeText() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= 5 && timeOfDay < 12) {
            return "Good Morning, ";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            return "Good Afternoon, ";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            return "Good Evening, ";
        } else {
            return "Good Night, ";
        }
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
            Intent goToLogInIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(goToLogInIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}