package com.laazer.sunshine.app;

import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.lang.Override;
import java.lang.Void;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        List<String> fakeData =  new ArrayList<String>(Arrays.asList(new String[]{"Today-Sunny-88/63", "Tomorrow-Sunny-88/63",
                "Tues-Sunny-88/63", "Wed-Sunny-88/63", "Thur-Sunny-88/63", "Fri-Sunny-88/63",
                "Sat-Sunny-88/63"}));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forcast,
                R.id.list_item_forecast_textview, fakeData);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

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
            new FetchWeatherTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
            final String QUERY_PARAM = "q";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, params[0]).
                    appendQueryParameter(UNITS_PARAM, "metric").appendQueryParameter(DAYS_PARAM, Integer.toString(7)).build();
            return HttpHelper.httpGet(builtUri.toString());
        }

    }
}