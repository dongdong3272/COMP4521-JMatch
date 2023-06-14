package edu.hkust.jmatch;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;
import java.util.UUID;

import edu.hkust.jmatch.backend.EmailVerificationTask;
import edu.hkust.jmatch.backend.Global;
import edu.hkust.jmatch.backend.PostRequest;

public class SignupActivity extends AppCompatActivity {

    Button candidateBtn, enterpriseBtn, verifyBtn;
    EditText usernameInput, emailInput, verificationInput, passwordInput;

    // Class variables
    private boolean isCandidate;
    private String username, email, verifyCode, password, generatedCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        candidateBtn = findViewById(R.id.candidateBtn);
        enterpriseBtn = findViewById(R.id.enterpriseBtn);
        verifyBtn = findViewById(R.id.verifyBtn);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        verificationInput = findViewById(R.id.verificationInput);
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

    public void sendVerification(View v) {
        System.out.println("try to send verification code");

        // Step 1: Check validity of the input (email)
        email = emailInput.getText().toString().trim();
        if (!validateInput(false)) return;

        // Step 2: Generate the verification code
        int randomNum = new Random().nextInt(900000) + 100000;
        generatedCode = Integer.toString(randomNum);

        // Step 3: Send the verification code
        final String requestBaseURL = Global.BaseURL.cloudFunctionURL;
        final String queryString = "";
        final String functionName = "email_verification";
        new EmailVerificationTask().execute(queryString, requestBaseURL, functionName, generatedCode, email);

        // Step 4: Display the Toast
        Toast.makeText(Global.context, "Verification Email Sent", Toast.LENGTH_SHORT).show();

        // Step 4: Disable the button
        verifyBtn.setEnabled(false);
    }


    public void fireSignUp(View v) {
        System.out.println("Try to sign up");

        // Step 1: Check validity of the input (username + email + password + verification code)
        username = usernameInput.getText().toString().trim();
        password = passwordInput.getText().toString().trim();
        verifyCode = verificationInput.getText().toString().trim();
        if (!validateInput(true)) return;

        // Step 2: Check if the account has been created ( = repeated username)
        checkExistingAccount(isCandidate);

        // Step 3: Write the new account to the database (In the processResult function)

        // Step 4: Display the Toast (In the processResult function)

        // Step 5: If valid, go to the Login Activity (In the processResult function)
    }

    private void checkExistingAccount(Boolean isCandidate) {
        String requestBaseURL = Global.BaseURL.queryBaseURL;
        String dbCollectionName = isCandidate ? "candidates" : "enterprises";
        String queryString = String.format("db.collection(\"%s\")" +
                        ".where({" +
                        "username: \"%s\"," +
                        "})" +
                        ".limit(1)" +
                        ".get()"
                , dbCollectionName, username);
        Log.d("Check Existing Account Query", queryString);

        new PostRequest(
                new PostRequest.ProcessRequestResult() {
                    @Override
                    public void processResult(JSONObject resultJSON) throws JSONException {
                        if (resultJSON.getJSONArray("data").length() == 1) {
                            Toast.makeText(Global.context, "Username Already Exists", Toast.LENGTH_SHORT).show();
                        } else {

                            // Step 3: Write the new account to the database
                            launchSignUpPostRequest(isCandidate);

                            // Step 4: Display the Toast
                            Toast.makeText(Global.context, "Successful Registration", Toast.LENGTH_SHORT).show();

                            // Step 5: If valid, go to the Login Activity
                            Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(loginIntent);
                        }
                    }
                }
        ).execute(queryString, requestBaseURL);
    }

    private void launchSignUpPostRequest(Boolean isCandidate) {
        String requestBaseURL = Global.BaseURL.addBaseURL;
        String dbCollectionName = isCandidate ? "candidates" : "enterprises";
        String queryString = String.format("" +
                        "db.collection(\"%s\").add({" +
                        "    data: [{" +
                        "        username: \"%s\"," +
                        "        email: \"%s\"," +
                        "        password: \"%s\"" +
                        "    }]" +
                        "})"
                , dbCollectionName, username, email, sha256Hex(password));
        Log.d("Sign In Query", queryString);
        new PostRequest().execute(queryString, requestBaseURL);
    }

    public void gotoLogin(View v) {
        Log.d("PageChange", "Go to Login Page");
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private boolean validateInput(boolean checkAll) {
        Context context = Global.context;
        int duration = Toast.LENGTH_SHORT;
        if (email.length() == 0) {
            Toast.makeText(context, "Please input email", duration).show();
            return false;
        }
        if (!checkAll) return true;
        if (username.length() == 0) {
            Toast.makeText(context, "Please input username", duration).show();
            return false;
        } else if (generatedCode.length() == 0) {
            Toast.makeText(context, "Please verify email", duration).show();
            return false;
        } else if (verifyCode.length() == 0) {
            Toast.makeText(context, "Please input verification", duration).show();
            return false;
        } else if (!generatedCode.equals(verifyCode)) {
            Toast.makeText(context, "Incorrect verification code", duration).show();
            return false;
        }
        return true;
    }

}