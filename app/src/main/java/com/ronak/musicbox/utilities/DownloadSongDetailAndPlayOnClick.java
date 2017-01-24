package com.ronak.musicbox.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ronak.musicbox.MainActivity;
import com.ronak.musicbox.MyService;
import com.ronak.musicbox.R;
import com.ronak.musicbox.model.CurrentStation;
import com.ronak.musicbox.model.Station;
import com.ronak.musicbox.network.DownloadContent;

import java.util.ArrayList;

public class DownloadSongDetailAndPlayOnClick extends AsyncTask<Void, Void, ArrayList<Uri>> {
    private static final String TAG = "DOWNLAODSONGDETAIL";
    ImageButton imageButtonPlay;
    String file;
    private Station station;
    private MainActivity activity;


    public DownloadSongDetailAndPlayOnClick(Station station, MainActivity activity) {
        this.station = station;
        this.activity = activity;
    }


    @Override
    protected void onPreExecute() {
        TextView textTitle, textDesc;

        super.onPreExecute();
        textTitle = (TextView) activity.findViewById(R.id.text_title);
        textDesc = (TextView) activity.findViewById(R.id.text_description);
        imageButtonPlay = (ImageButton) activity.findViewById(R.id
                .but_media_play);

        CurrentStation currentStation = CurrentStation.getCurrentStation();
        if (currentStation == null) {
            currentStation = new CurrentStation(station);
            CurrentStation.save(currentStation);
        } else {
            currentStation.setKey("121");
            currentStation.setName(station.getName());
            currentStation.setCst(station.getCst());
            currentStation.setBrbitrate(station.getBrbitrate());
            currentStation.setCtqueryString(station.getCtqueryString());
            currentStation.setGenre(station.getGenre());
            currentStation.setGenre3(station.getGenre3());
            currentStation.setGenre2(station.getGenre2());
            currentStation.setLc(station.getLc());
            currentStation.setLogo(station.getLogo());
            currentStation.setMl(station.getMl());
            currentStation.setUriArrayList(station.getUriArrayList());
            currentStation.setMt(station.getMt());
            currentStation.setStationId(station.getStationId());

            currentStation.save();
        }

//        currentStation.save();


        if (textDesc != null && textTitle != null) {
            textDesc.setText(currentStation.getCtqueryString());
            textTitle.setText(currentStation.getName());
        }
    }

    @Override
    protected ArrayList<Uri> doInBackground(Void... params) {
        ArrayList<String> m3u = DownloadContent.lineArray("http://yp.shoutcast" +
                ".com/" + "/sbin/tunein-station.m3u" + "?id=" + this.station.getStationId());
        ArrayList<Uri> uriArrayList = new ArrayList<Uri>();

        for (int i = 0; i < m3u.size(); i++) {
            if (m3u.get(i).startsWith("http")) {
                file = m3u.get(i);
                file = file.replace("http", "icy");
                uriArrayList.add(Uri.parse(file));
                break;
            }
        }

        return uriArrayList;
    }

    @Override
    protected void onPostExecute(final ArrayList<Uri> uriArrayList) {
        super.onPostExecute(uriArrayList);
        if (imageButtonPlay != null) {
            imageButtonPlay.setEnabled(true);
        }
        if (uriArrayList != null) {
            station.setUriArrayList(uriArrayList); //Must be not null
        }
        bindWithServiceAndExecute(station);
    }

    private void bindWithServiceAndExecute(final Station station) {
        Intent intent = new Intent(activity, MyService.class);
        activity.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MyService.ServiceBinder servicBinder = (MyService.ServiceBinder) iBinder;
                MyService myServiceEngine = servicBinder.getService();
//  				myServiceEngine.prepare(url,"icy://37.130.230.93:9092");
                try {
                    myServiceEngine.prepare(station);
                } catch (Exception ex) {
                    Log.e("Exception service", "while preparing for" + getClass().getName());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.e("disconnected", "service");
            }
        }, Context.BIND_AUTO_CREATE);

    }
}