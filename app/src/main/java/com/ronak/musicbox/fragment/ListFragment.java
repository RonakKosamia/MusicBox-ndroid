package com.ronak.musicbox.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ronak.musicbox.R;
import com.ronak.musicbox.flags.Flags;
import com.ronak.musicbox.flags.Url_format;
import com.ronak.musicbox.model.Station;
import com.ronak.musicbox.network.DownloadContent;
import com.ronak.musicbox.parser.ParserXMLtoJSON;
import com.ronak.musicbox.parser.StationList;
import com.ronak.musicbox.parser.TuneIn;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
@Deprecated
public class ListFragment extends Fragment {


    private String url;
    private SharedPreferences preferences;

//    execute(url);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        preferences = getActivity().getSharedPreferences(Flags.SETTINGS, Context
                .MODE_PRIVATE);

        return inflater.inflate(R.layout.station_list, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshThisList(view);
    }

    public void refreshThisList(View view) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (preferences != null) {
            url = preferences.getString(Flags.JSON_URL, new Url_format().getTopStationsXML(Flags.DEV_ID, "0", "50",
                    null, null));
        } else {
            preferences = getActivity().getSharedPreferences(Flags.SETTINGS, Context.MODE_PRIVATE);
            url = preferences.getString(Flags.JSON_URL, new Url_format().getTopStationsXML(Flags.DEV_ID, "0", "50",
                    null, null));
        }

        if (DownloadContent.isAvailable(getActivity())) {

            DownloadAndShowList downloadAndShowList = new DownloadAndShowList(getView());
            downloadAndShowList.execute(url);

        } else {
            Toast.makeText(getActivity(), "Network connection not available", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Class downloads the list and shows on the ListView
     */
    private class DownloadAndShowList extends AsyncTask<String, Void, ArrayList<Station>> {
        //        ImageButton listButton;
        StationList stationList;
        RecyclerView listView;
        ProgressBar progressBar;
        ParserXMLtoJSON parser;
        Url_format url_format;
        TuneIn tuneIn;
        private View view;

        DownloadAndShowList(@NonNull View view) {
            this.view = view;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = (ProgressBar) view.findViewById(R.id.progressbarlist);
            progressBar.setVisibility(View.VISIBLE);
            parser = new ParserXMLtoJSON();
            url_format = new Url_format();
            listView = (RecyclerView) view.findViewById(R.id.list_stations);

//            listButton = (ImageButton) view.getRootView().findViewById(R.id.but_media_list);
//            if (listButton != null) {
//                listButton.setEnabled(false);
//            }


        }

        @Override
        protected ArrayList<Station> doInBackground(String... params) {
            try {
                String data = DownloadContent.downloadContent(params[0]);
                stationList = parser.getTopStationswithLIMIT(data);
                tuneIn = stationList.getTuneIn();
                if (stationList != null && stationList.getArrayListStations() != null) {
                    Collections.reverse(stationList.getArrayListStations());
                    return stationList.getArrayListStations();
                } else return null;

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Station> stations) {

            if (listView != null && stations != null && stations.size() > 0) {
                super.onPostExecute(stations);

//                recyclerView.setAdapter(new ListAdapterStations(getActivity().getApplicationContext(), R.layout.itemlistrow,
//                        stations));
                AdapterStationsList adapterStationsList = new AdapterStationsList(getActivity(), stations);
                listView.setAdapter(adapterStationsList);
                listView.setLayoutManager(new LinearLayoutManager(getContext()));
//                if (listButton != null) {
//                    listButton.setEnabled(true);
//                }
                progressBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(getContext(), "No station found", Toast.LENGTH_LONG).show();
                url = preferences.getString(Flags.JSON_URL, new Url_format().getTopStationsXML(Flags.DEV_ID, "0", "50",
                        null, null));
                if (progressBar != null && progressBar.isShown()) {
                    progressBar.setVisibility(View.GONE);
                }

            }

        }


    }
}
