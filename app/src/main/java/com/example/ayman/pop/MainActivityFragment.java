package com.example.ayman.pop;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
public class MainActivityFragment extends Fragment {


    private GridView mGridView;
    private ProgressBar mProgressBar;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private String apiurl = "http://api.themoviedb.org/3/discover/movie?sort_by=";
    private String imgurl = "http://image.tmdb.org/t/p/w185";
    FavDataBase my;
    Boolean favBool;
    String sort;
    SharedPreferences sharedPrefs;
    int mPostion=GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    GridItem item;


    public MainActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_grid_view, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                item = (GridItem) parent.getItemAtPosition(position);
                ((Callback) getActivity()).onItemSelected(item.getMov());
                //  Intent intent = new Intent(getContext(), DetailsActivity.class);
                // intent.putExtra("movieDetails", item.getMov());
                // startActivity(intent);
                mPostion = position;
            }
        });
        run();
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPostion = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPostion != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPostion);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //run();
        String msort = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        if(!msort.equals(sort)) run();
        Boolean t=sharedPrefs.getBoolean("twoPane",false);
        if(t) {
            sharedPrefs.edit().putBoolean("twoPane", false).apply();
            run();
        }

    }

    public void run() {
        mPostion=GridView.INVALID_POSITION;
        sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        sort = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        String tit;
        if (sort.equals("fav")) tit = "My Favorites";
        else if (sort.equals("most-popular")) tit = "Most Popular";
        else tit = "Highest Rated";
        getActivity().setTitle(tit);
        mGridData = new ArrayList<>();
        favBool = false;
        if (sort.equals("fav")) favBool = true;
        mGridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, mGridData, favBool);
        mGridView.setAdapter(mGridAdapter);

        mProgressBar.setVisibility(View.VISIBLE);
        if (sort.equals("fav")) {
            my = new FavDataBase(getActivity());
            ArrayList<Movie> arr;
            arr = my.getAllMovies();
            GridItem item;
            for (int i = 0; i < arr.size(); i++) {
                item = new GridItem();
                item.setImage(arr.get(i).getOffline_path());
                item.setMov(arr.get(i));
                mGridData.add(item);
            }
            mGridAdapter.setGridData(mGridData);
            mProgressBar.setVisibility(View.GONE);
            if (mPostion != GridView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                mGridView.setSelection(mPostion);
            }
        } else new AsyncHttpTask().execute(apiurl);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {


        private int getDataFromJson(String JsonStr)
                throws JSONException

        {
            ArrayList<Movie> arr = new ArrayList<>();

            JSONObject jsonRootObject = new JSONObject(JsonStr);
            JSONArray jsonArray = jsonRootObject.optJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                Movie mov = new Movie();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                mov.setOriginal_title(jsonObject.optString("original_title"));
                mov.setOverview(jsonObject.optString("overview"));
                mov.setPoster_path(jsonObject.optString("poster_path"));
                mov.setRelease_date(jsonObject.optString("release_date"));
                mov.setVote_average(jsonObject.optDouble("vote_average"));
                mov.setOffline_path(jsonObject.optString("poster_path"));
                mov.setId(jsonObject.optInt("id"));
                arr.add(mov);
            }
            return parseResult(arr);
        }

        @Override
        protected Integer doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            String jsonString = null;
            String finalapi = null;
            try {
                String finsort = null;
                if (sort.equals("most-popular")) finsort = "popularity.desc";
                else finsort = "vote_count.desc";
                finalapi = params[0] + finsort + "&api_key=" + BuildConfig.apikey;
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
                Log.e("Error in line 197", "error in line 197");
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Error in line 207", "Error closing stream", e);
                    }
                }
            }
            try {
                return getDataFromJson(jsonString);
            } catch (JSONException e) {
                Log.e("Error in line 214", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(Integer result) {
            if (result != null) {
                mGridAdapter.setGridData(mGridData);
            } else {
                if (isNetworkAvailable(getContext()))
                    Toast.makeText(getActivity(), "Failed to fetch data! Try again", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Failed to fetch data! Check connection", Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
            if (mPostion != GridView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                mGridView.setSelection(mPostion);
            }
        }

    }

    private int parseResult(ArrayList<Movie> arr) {

        GridItem item;
        for (int i = 0; i < arr.size(); i++) {
            item = new GridItem();
            item.setImage(imgurl + arr.get(i).getPoster_path());
            item.setMov(arr.get(i));
            mGridData.add(item);
        }
        return arr.size();
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie m);
    }
    void onLocationChanged( ) {
        item=null;
        run();
       // getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }
}