package edu.hkust.jmatch;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import static edu.hkust.jmatch.NLP.TextMatching.cleanString;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import edu.hkust.jmatch.backend.Global;
import edu.hkust.jmatch.backend.PostRequest;

public class SubmitMatchActivity extends AppCompatActivity {
    EditText titleInput, fileInput, textInput;
    Button uploadBtn, getMatchResultBtn;
    InputStream inputStream;
    String textToBeMatched;
    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_match);
        titleInput = findViewById(R.id.titleInput);
        fileInput = findViewById(R.id.fileInput);
        textInput = findViewById(R.id.textInput);
        uploadBtn = findViewById(R.id.uploadBtn);
        getMatchResultBtn = findViewById(R.id.getMatchResultBtn);
        textToBeMatched = "";

        // Set the Backward button on Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize result launcher
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            fileInput.setText(uri.getPath());
                            textToBeMatched = extractTextPdfFile(uri);
                        }
                    }
                });

    }

    public void uploadPDF(View v) {
        System.out.println("upload PDF");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        resultLauncher.launch(intent);
    }

    private String extractTextPdfFile(Uri uri) {
        try {
            inputStream = SubmitMatchActivity.this.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String fileContent = "";
        StringBuilder builder = new StringBuilder();
        PdfReader reader = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                reader = new PdfReader(inputStream);
                int pages = reader.getNumberOfPages();
                for (int i = 1; i <= pages; i++) {
                    fileContent = PdfTextExtractor.getTextFromPage(reader, i);
                    builder.append(fileContent);
                }
            }
            reader.close();
            return cleanString(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void fireMatch(View v) throws IOException {
        // Step 1: Check validity of the input (title + document(file/textInput))
        if (!validateInput()) return;
        if (textToBeMatched.length() == 0)
            textToBeMatched = textInput.getText().toString().trim();
        System.out.println(textToBeMatched);

        // Step 2: Write the new document record to the database
        launchSubmitMatchPostRequest();

        // Step 3: If valid, go to the MatchResultActivity (In the processResult function)
    }

    private void launchSubmitMatchPostRequest() {
        final String title = titleInput.getText().toString().trim();
        Global.Document.documentID = UUID.randomUUID().toString();
        String requestBaseURL = Global.BaseURL.addBaseURL;
        String dbCollectionName = Global.User.isCandidate ? "cvs" : "jds";
        SimpleDateFormat sdf = new SimpleDateFormat("YY/MM/dd 'at' HH:mm");
        String currentTime = sdf.format(new Date());
        String queryString = String.format("" +
                        "db.collection(\"%s\").add({" +
                        "    data: [{" +
                        "        documentID: \"%s\"," +
                        "        time: \"%s\"," +
                        "        username: \"%s\"," +
                        "        title: \"%s\"," +
                        "        content: \"%s\"" +
                        "    }]" +
                        "})"
                , dbCollectionName, Global.Document.documentID, currentTime, Global.User.username, title, textToBeMatched);
        Log.d("Add Record Query", queryString);
        new PostRequest(
                new PostRequest.ProcessRequestResult() {
                    @Override
                    public void processResult(JSONObject resultJSON) throws JSONException {
                        // Step 3: If valid, go to the MatchResultActivity
                        Intent goToResult = new Intent(SubmitMatchActivity.this, KeywordMatchActivity.class);
                        startActivity(goToResult);
                    }
                }
        ).execute(queryString, requestBaseURL);
    }

    private boolean validateInput() {
        Context context = Global.context;
        int duration = Toast.LENGTH_SHORT;
        if (titleInput.getText().toString().length() == 0) {
            Toast.makeText(context, "Please input title", duration).show();
            return false;
        } else if (textToBeMatched.length() == 0 && textInput.getText().toString().length() == 0) {
            Toast.makeText(context, "Please upload document", duration).show();
            return false;
        }
        return true;
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
            Intent goToLogInIntent = new Intent(SubmitMatchActivity.this, LoginActivity.class);
            startActivity(goToLogInIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}