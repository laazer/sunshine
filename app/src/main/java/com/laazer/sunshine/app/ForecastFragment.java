package com.laazer.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
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
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.Override;
import java.lang.Void;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A placeholder fragment containing a simple view.
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
        List<String> weekForecast =  new ArrayList<String>(Arrays.asList(new String[]{"Today-Sunny-88/63", "Tomorrow-Sunny-88/63",
                "Tues-Sunny-88/63", "Wed-Sunny-88/63", "Thur-Sunny-88/63", "Fri-Sunny-88/63",
                "Sat-Sunny-88/63"}));
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
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
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forcastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.refresh) {
            SharedPreferences textPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String zipPref = textPreference.getString(getString(R.string.pref_zip_code_entry_key), "02115");
            String unit = textPreference.getString(getString(R.string.pref_pick_unit_key), "imperial");
            new FetchWeatherTask().execute(zipPref, unit);
            return true;
        }
        if (id == R.id.view_location) {
            SharedPreferences textPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String zipPref = textPreference.getString(getString(R.string.pref_zip_code_entry_key), "02115");
            showMap(Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", zipPref).build());
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private String getForecastString(String param, String unit) {
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, param).
                    appendQueryParameter(UNITS_PARAM, unit).appendQueryParameter(DAYS_PARAM, Integer.toString(7)).build();
            String result =  HttpHelper.httpGet(builtUri.toString());
            Log.v("Built url", result);
            return result;
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {
            try {
                return getWeatherDataFromJson(getForecastString(params[0], params[1]), 7);
            }catch (JSONException e) {
                Log.e("getForecast", e.toString());
                return null;
            }

        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result != null) {
                mForecastAdapter.clear();
                for(String s : result) {
                    mForecastAdapter.add(s);
                }
            }
        }



    }


}