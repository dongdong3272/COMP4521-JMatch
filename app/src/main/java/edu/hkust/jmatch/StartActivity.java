package edu.hkust.jmatch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import edu.hkust.jmatch.backend.Global;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize data first
        initializeData();

        // Set the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void goToLogin(View v) {
        Log.d("PageChange", "Go to Login Page");
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    public void initializeData() {
        Global.context = this;
        Global.retrieveAccessToken();
        checkUserStatus();
    }


    private void checkUserStatus() {
        // Check whether there is a user account logged with this device
        String requestBaseURL = Global.BaseURL.queryBaseURL;
        // Read the Android ID of this device and get the user info
//        String queryString = String.format("db.collection(\"users\")" +
//                ".where({androidID: \"%s\"})" +
//                ".limit(100).get()", androidID);
    }
}