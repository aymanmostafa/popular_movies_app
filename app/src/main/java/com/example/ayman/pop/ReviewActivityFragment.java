package com.example.ayman.pop;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReviewActivityFragment extends Fragment {

    TextView rev;
    int id;
    String finalres;
    private ProgressBar mProgressBar;

    public ReviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_review, container, false);
        rev = (TextView) rootView.findViewById(R.id.reviewview_id);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        Intent i = getActivity().getIntent();
        id = i.getIntExtra("movierev", 0);
        finalres = null;
        mProgressBar.setVisibility(View.VISIBLE);
        new AsyncHttpTask().execute(String.valueOf(id), "reviews");
        return rootView;
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {


        private int getDataFromJsonReviews(String JsonStr)
                throws JSONException

        {
            ArrayList<String> arr = new ArrayList<>();

            JSONObject jsonRootObject = new JSONObject(JsonStr);
            JSONArray jsonArray = jsonRootObject.optJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                arr.add("Written by: " + jsonObject.optString("author") + "\n" + jsonObject.optString("content"));
            }
            return parseResultrev(arr);
        }

        @Override
        protected Integer doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            String jsonString = null;
            String finalapi = null;
            try {
                finalapi = "http://api.themoviedb.org/3/movie/" + params[0] + "/" + params[1] + "?api_key=" + BuildConfig.apikey;
                Uri builtUri = Uri.parse(finalapi).buildUpon().build();

                URL url = new URL(builtUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonString = buffer.toString();
            } catch (IOException e) {
                Log.e("Error in line 119", "error in line 119");
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Error in line 129", "Error closing stream", e);
                    }
                }
            }
            try {
                return getDataFromJsonReviews(jsonString);
            } catch (JSONException e) {
                Log.e("Error in line 136", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(Integer result) {
            if (finalres == null || finalres.equals("")) {
                if (isNetworkAvailable(getContext())) finalres = "No Reviews found!Try later";
                else finalres = "Check connection & Try again";
            }
            rev.setText(finalres);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private int parseResultrev(ArrayList<String> arr) {

        StringBuilder storage = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            storage.append(arr.get(i) + "\n\n-------------------------------------------------\n\n");
        }
        finalres = storage.toString();
        return arr.size();
    }

}
