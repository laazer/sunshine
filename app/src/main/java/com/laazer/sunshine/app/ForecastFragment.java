package com.laazer.sunshine.app;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Menu refresh = inflater.inflate(android.R.menu.forcastfragemnt, false);
        List<String> fakeData =  new ArrayList<String>(Arrays.asList(new String[]{"Today-Sunny-88/63", "Tomorrow-Sunny-88/63",
                "Tues-Sunny-88/63", "Wed-Sunny-88/63", "Thur-Sunny-88/63", "Fri-Sunny-88/63",
                "Sat-Sunny-88/63"}));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item.forcast,
                R.id.list_item_forcast_view, fakeData);
        ListView lv = (ListView) onCreateView(rootView.findViewById(R.id.listView));
        lv.setAdapter(adapter);

        return rootView;
    }
    public class FetchWeatherTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            return HttpHelper.httpGet("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
        }
    }
}