package edu.hkust.jmatch.backend;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PostRequest extends AsyncTask<String, Void, String> {

    ProcessRequestResult processRequestResult = null;

    /*
    *  This is a function that will run in onPostExecute
    * */
    public interface ProcessRequestResult {
        void processResult(JSONObject resultJSON) throws JSONException;
    }

    public PostRequest() {
        super();
    }

    public PostRequest(ProcessRequestResult prr) {
        super();
        this.processRequestResult = prr;
    }


    @Override
    protected void onPreExecute() {
        // TODO: Implement a loading animation
    }


    /*
     *  @Params queryStrings: an array of strings
     *           queryStrings[0]: query string
     *           queryStrings[1]: request base URL
     * */
    @Override
    protected String doInBackground(String... queryStrings) {
        //https://api.weixin.qq.com/tcb/databasequery?access_token=ACCESS_TOKEN

        final String ACCESS_TOKEN = "access_token";
        final String queryString = queryStrings[0];
        final String requestBaseURL = queryStrings[1];

        while (Global.accessToken == "") ;
        Log.d("Global Access Token", Global.accessToken);

        Uri uri = Uri.parse(requestBaseURL).buildUpon()
                .appendQueryParameter(ACCESS_TOKEN, Global.accessToken)
                .build();
        String uriString = uri.toString();
        HttpsURLConnection conn = null;
        InputStream inputStream = null;

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
            postJson.put("env", Global.env);
            postJson.put("query", queryString);
            Log.d("Post Json", postJson.toString());
            String postJsonString = postJson.toString();
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(postJsonString.getBytes());
            outputStream.flush();

            // Connect
            conn.connect();
            inputStream = conn.getInputStream();
            String contentString = convertInputToString(inputStream);

            // Get response
            int response = conn.getResponseCode();
            Log.d("responseCode: ", Integer.toString(response));
            return contentString;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed";
        } finally {
            // After all executions, disconnect the connection, close the inputStream
            conn.disconnect();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onPostExecute(String result) {
        // TODO: Stop a loading animation
        try {
            JSONObject response = new JSONObject(result);
            Log.d("Post Execute Result: ", result);
            if(processRequestResult != null) processRequestResult.processResult(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String convertInputToString(InputStream stream) throws IOException {
        return IOUtils.toString(stream, "UTF-8");
    }
}
