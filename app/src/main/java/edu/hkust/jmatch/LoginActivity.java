package edu.hkust.jmatch;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.hkust.jmatch.backend.Global;
import edu.hkust.jmatch.backend.PostRequest;

public class LoginActivity extends AppCompatActivity {

    Button candidateBtn, enterpriseBtn;
    EditText usernameInput, passwordInput;
    private boolean isCandidate;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        candidateBtn = findViewById(R.id.candidateBtn);
        enterpriseBtn = findViewById(R.id.enterpriseBtn);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        isCandidate = true;
    }

    public void switchCandidate(View v) {
        isCandidate = true;
        candidateBtn.setBackgroundColor(Color.BLACK);
        candidateBtn.setTextColor(Color.WHITE);
        enterpriseBtn.setBackgroundColor(0xFFC8C4B7);
        enterpriseBtn.setTextColor(Color.BLACK);
    }

    public void switchEnterprise(View v) {
        isCandidate = false;
        enterpriseBtn.setBackgroundColor(Color.BLACK);
        enterpriseBtn.setTextColor(Color.WHITE);
        candidateBtn.setBackgroundColor(0xFFC8C4B7);
        candidateBtn.setTextColor(Color.BLACK);
    }

    public void fireLogIn(View v) {
        System.out.println("try to log in");

        // Step 1: Check validity of the input (username + password)
        username = usernameInput.getText().toString().trim();
        password = passwordInput.getText().toString().trim();
        if (!validateInput()) return;

        // Step 2: Query the database to check whether there is a matched user
        launchLoginPostRequest(isCandidate);

        // Step 3: If valid, go to the MainActivity (In the processResult function)
    }

    private void launchLoginPostRequest(Boolean isCandidate) {
        String requestBaseURL = Global.BaseURL.queryBaseURL;
        String dbCollectionName = isCandidate ? "candidates" : "enterprises";
        String queryString = String.format("db.collection(\"%s\")" +
                        ".where({" +
                        "username: \"%s\"," +
                        "password: \"%s\"" +
                        "})" +
                        ".limit(1)" +
                        ".get()"
                , dbCollectionName, username, sha256Hex(password));
        Log.d("Log In Query", queryString);

        new PostRequest(
                new PostRequest.ProcessRequestResult() {
                    @Override
                    public void processResult(JSONObject resultJSON) throws JSONException {
                        boolean isLoginSuccess = resultJSON.getJSONArray("data").length() == 1;

                        if (isLoginSuccess) {
                            // Update global variable
                            String dataJSONString = resultJSON.getJSONArray("data").getString(0);
                            JSONObject dataJSON = new JSONObject(dataJSONString);
                            Global.User.loggedIn = true;
                            Global.User.username = dataJSON.getString("username");
                            Global.User.isCandidate = isCandidate;

                            // Step 3: If valid, go to the MainActivity
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                        } else {
                            Toast.makeText(Global.context, "Incorrect Username/Password", Toast.LENGTH_SHORT).show();
                            Global.User.loggedIn = false;
                        }
                    }
                }
        ).execute(queryString, requestBaseURL);
    }

    public void gotoSignUp(View v) {
        Log.d("PageChange", "Go to SignUp Page");
        Intent signupIntent = new Intent(this, SignupActivity.class);
        startActivity(signupIntent);
    }

    private boolean validateInput() {
        Context context = Global.context;
        int duration = Toast.LENGTH_SHORT;
        if (username.length() == 0) {
            Toast.makeText(context, "Please input username", duration).show();
            return false;
        } else if (password.length() == 0) {
            Toast.makeText(context, "Please input password", duration).show();
            return false;
        }
        return true;
    }

}