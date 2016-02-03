package com.example.ayman.pop;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment implements View.OnClickListener {

    TextView title, year, rate, overview;
    ImageView poster;
    Movie mov;
    Button rev, fav;
    String url = "http://image.tmdb.org/t/p/w185";
    Button tr;
    ProgressDialog PD;
    FavDataBase my;
    String res;
    SharedPreferences sharedPrefs;
    String sort;
    ArrayList<Trailer> finalres;
    RelativeLayout lay;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    Boolean twoPane;

    public DetailsActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        res = null;
        fav = (Button) rootView.findViewById(R.id.favor_id);
        fav.setOnClickListener(this);
        Intent i = getActivity().getIntent();
        if(i==null||i.getSerializableExtra("movieDetails")==null){
            Bundle arguments = getArguments();
            if (arguments != null) {
                mov =(Movie) arguments.getSerializable("movieDetails");
            }
            twoPane=true;
        }
        else  {
            mov = (Movie) i.getSerializableExtra("movieDetails");
            twoPane=false;
        }
        sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        sort = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        lay=(RelativeLayout) rootView.findViewById(R.id.allViewsLayout_id);
        title = (TextView) rootView.findViewById(R.id.title_id);
        year = (TextView) rootView.findViewById(R.id.year_id);
        rate = (TextView) rootView.findViewById(R.id.rate_id);
        overview = (TextView) rootView.findViewById(R.id.overview_id);

        poster = (ImageView) rootView.findViewById(R.id.poster_id);

        rev = (Button) rootView.findViewById(R.id.review_id);
        rev.setOnClickListener(this);
        fav = (Button) rootView.findViewById(R.id.favor_id);
        fav.setOnClickListener(this);


        tr = (Button) rootView.findViewById(R.id.trail_id);
        tr.setOnClickListener(this);
        registerForContextMenu(tr);
        tr.setLongClickable(false);

        if(mov!=null){
            title.setText(mov.getOriginal_title());
            if (mov.getRelease_date().length() >= 4)
                year.setText(mov.getRelease_date().substring(0, 4));
            else year.setText(mov.getRelease_date());
            rate.setText(String.valueOf(mov.getVote_average()) + "/10");
            overview.setText("\n" + mov.getOverview());


            if (sort.equals("fav")) {
                fav.setText("Remove from favorite");
                poster.setImageBitmap(StringToBitMap(mov.getOffline_path()));
            } else {
                res = url + mov.getPoster_path();
                Picasso.with(getContext()).load(res).into(poster);
            }
        }
        else{
            hidden();
        }


        finalres = null;

        return rootView;
    }
    void onLocationChanged() {
              hidden();
            }

    void hidden(){
        lay.setVisibility(View.INVISIBLE);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == rev) {
            if (!isNetworkAvailable(getContext()))
                Toast.makeText(getActivity(), "Check connection & Try again", Toast.LENGTH_SHORT).show();
            else {
                Intent intent = new Intent(getContext(), ReviewActivity.class);
                intent.putExtra("movierev", mov.getId());
                startActivity(intent);
            }
        } else if (v == fav) {
            my = new FavDataBase(getActivity());
            if (sort.equals("fav")) {
                my.deleteMovieByID(mov.getId());
                Toast.makeText(
                        getActivity(), "Removed from Favourite list", Toast.LENGTH_SHORT
                ).show();

                sharedPrefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
                if(twoPane){
                    getActivity().finish();
                    startActivity(new Intent(getContext(), MainActivity.class));
                }
                else{
                    sharedPrefs.edit().putBoolean("twoPane", true).apply();
                }

            } else {
                Bitmap bm = ((BitmapDrawable) poster.getDrawable()).getBitmap();
                mov.setOffline_path(BitMapToString(bm));
                my.addMovie(mov);
                Toast.makeText(
                        getActivity(), "Added to Favourite list", Toast.LENGTH_SHORT
                ).show();
            }
        } else if (v == tr) {
            PD = new ProgressDialog(getActivity());
            PD.setTitle("Please Wait..");
            PD.setMessage("Loading...");
            PD.show();
            new AsyncHttpTask().execute(String.valueOf(mov.getId()), "videos", String.valueOf(v.getId()));
        }

    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Choose Trailer");
        for (int i = 0; i < finalres.size(); i++)
            menu.add(finalres.get(i).toString());
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://www.youtube.com/watch?v=" + finalres.get(item.getItemId()).getKey()));
        startActivity(i);

        return super.onContextItemSelected(item);
    }

    // get this function from stackoverflow
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    public class AsyncHttpTask extends AsyncTask<String, Void, String> {


        private String getDataFromJsonVideos(String JsonStr, String v)
                throws JSONException

        {
            ArrayList<Trailer> arr = new ArrayList<>();
            JSONObject jsonRootObject = new JSONObject(JsonStr);

            JSONArray jsonArray = jsonRootObject.optJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                Trailer t = new Trailer();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                t.setId(jsonObject.optString("id"));
                t.setKey(jsonObject.optString("key"));
                t.setName(jsonObject.optString("name"));
                arr.add(t);
            }
            finalres = arr;
            return v;
        }


        @Override
        protected String doInBackground(String... params) {

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
                Log.e("Error in line 248", "error in line 248");
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Error in line 258", "Error closing stream", e);
                    }
                }
            }
            try {
                return getDataFromJsonVideos(jsonString, params[2]);
            } catch (JSONException e) {
                Log.e("Error in line 265", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(String v) {
            PD.dismiss();
            if (isNetworkAvailable(getContext())) {
                if (finalres == null || finalres.size() == 0)
                    Toast.makeText(getActivity(), "No Trailer found!Try later", Toast.LENGTH_SHORT).show();
                else {
                    getActivity().openContextMenu(getActivity().findViewById(Integer.valueOf(v)));
                }
            } else {
                Toast.makeText(getActivity(), "Check connection & Try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // get this function from stackoverflow
    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    // get this function from stackoverflow
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
