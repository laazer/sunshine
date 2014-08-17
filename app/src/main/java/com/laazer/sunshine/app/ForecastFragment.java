package com.laazer.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.Override;
import java.lang.Void;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A placeholder fragment containing a the 7 Day Forecast
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    ArrayAdapter<String> mForecastAdapter;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Fake data for ArrayAdapter for testing
        List<String> fakeForecast =  new ArrayList<String>(Arrays.asList(new String[]{"Today-Sunny-88/63", "Tomorrow-Sunny-88/63",
                "Tues-Sunny-88/63", "Wed-Sunny-88/63", "Thur-Sunny-88/63", "Fri-Sunny-88/63",
                "Sat-Sunny-88/63"}));
        List<String> weekForecast = new ArrayList<String>();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        //set ArrayAdapter
        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forcast,
                R.id.list_item_forecast_textview, weekForecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Context context = view.getContext();
                CharSequence text = adapterView.getAdapter().getItem(i).toString();
                Intent detailIntent = new Intent(context, DetailActivity.class);
                detailIntent.putExtra("forecast", text);
                startActivity(detailIntent);
            }
        });
        updateWeather();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forcastfragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Retreives the updated weather from the db
     */
    private void updateWeather() {
        String location = Utility.getPreferredLocation(getActivity());
        new FetchWeatherTask(getActivity(), mForecastAdapter).execute(location);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.refresh) {
            updateWeather();
            return true;
        }
        if (id == R.id.view_location) {
            SharedPreferences textPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
            //get zip code for map call
            String zipPref = textPreference.getString(getString(R.string.pref_zip_code_entry_key), "02115");
            showMap(Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", zipPref).build());
        }
        return super.onOptionsItemSelected(item);
    }

    //Start explicit intent to display location
    private void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}