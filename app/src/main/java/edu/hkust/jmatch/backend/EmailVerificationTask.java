package edu.hkust.jmatch.backend;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class EmailVerificationTask extends AsyncTask<String, Void, Void> {

    @Override
    protected void onPreExecute() {
        // TODO: Implement a loading animation
    }

    /*
     *  @Params queryStrings: an array of strings
     *           queryStrings[0]: query string
     *           queryStrings[1]: request base URL
     *           queryStrings[2]: function name
     *           queryStrings[3]: code
     *           queryStrings[3]: email
     * */
    @Override
    protected Void doInBackground(String... queryStrings) {
        //https://api.weixin.qq.com/tcb/databasequery?access_token=ACCESS_TOKEN

        final String ACCESS_TOKEN = "access_token";
        final String queryString = queryStrings[0];
        final String requestBaseURL = queryStrings[1];
        final String functionName = queryStrings[2];
        final String code = queryStrings[3];
        final String email = queryStrings[4];

        while (Global.accessToken == "") ;
        Log.d("Global Access Token", Global.accessToken);

        Uri uri = Uri.parse(requestBaseURL).buildUpon()
                .appendQueryParameter(ACCESS_TOKEN, Global.accessToken)
                .appendQueryParameter("env", Global.env)
                .appendQueryParameter("name", functionName)
                .build();
        String uriString = uri.toString();
        HttpsURLConnection conn = null;

        try {
            // Connection Configuration
            URL url = new URL(uriString);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(100000);
            conn.setConnectTimeout(100000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Input parameters
            JSONObject postJson = new JSONObject();
            postJson.put("code", code);
            postJson.put("email", email);
            String postJsonString = postJson.toString();
            Log.d("postJson String", postJsonString);
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(postJsonString.getBytes());
            outputStream.flush();

            // Connect
            conn.connect();

            // Get response
            int response = conn.getResponseCode();
            Log.d("responseCode", Integer.toString(response));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return null;
    }


    protected void onPostExecute(String result) {
        // TODO: Stop a loading animation
    }
}




