package edu.hkust.jmatch.backend;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AccessToken extends AsyncTask<Void, Void, Void> {

    // AccessToken class is for getting access token
    // Access Token is needed for all database operations later on

    // REQUEST METHOD: GET
    // REQUEST URL: https://api.weixin.qq.com/cgi-bin/token?
    // PARAMETERS: grant_type=client_credential&appid=APPID&secret=APPSECRET

    final String requestBaseURL = "https://api.weixin.qq.com/cgi-bin/token?";
    final String GRANT_TYPE = "grant_type";
    final String APPID = "appid";
    final String APPSECRET = "secret";

    public class AccessTokenScheduler extends Thread {

        // AccessTokenScheduler will retrieve the accessToken every 2 hours
        // because the accessToken is valid for 2 hours upon requested
        public void run() {
            try {
                Thread.sleep(7195000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Global.accessToken = "";
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new AccessToken().execute();
        }
    }

    // Reads an InputStream and converts it to a String.
    public String convertInputToString(InputStream stream) throws IOException {
        return IOUtils.toString(stream, "UTF-8");
    }


    @Override
    protected Void doInBackground(Void... voids) {
        System.out.println("doInBackground");

        // The authentication information of JMatch app
        Uri uri = Uri.parse(requestBaseURL).buildUpon()
                .appendQueryParameter(GRANT_TYPE, "client_credential")
                .appendQueryParameter(APPID, "wxc6f81a57d72aaceb")
                .appendQueryParameter(APPSECRET, "ccf2b1ad37bb12b0524dff6f10f2f630")
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

            // Connect
            conn.connect();

            // Get response
            int response = conn.getResponseCode();
            Log.d("Connection", "The response is: " + response);

            // Get result
            inputStream = conn.getInputStream();
            String contentString = convertInputToString(inputStream);

            try {
                // Convert JSON String to JSON object
                JSONObject resultResponse = new JSONObject(contentString);
                // Get the accessToken from the javascript object
                String accessToken = resultResponse.getString("access_token");
                Global.accessToken = accessToken;
                Log.d("Access Token", Global.accessToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // After we get the access token (or even we fail), disconnect the connection
            conn.disconnect();

            // Try to close the inputStream
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Create a new AccessTokenScheduler thread to refresh accessToken every 2 hours
            Thread nextAccessToken = new AccessTokenScheduler();
            nextAccessToken.start();
        }
        return null;
    }

}
