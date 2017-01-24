package com.ronak.musicbox.network;

import android.os.AsyncTask;

import com.ronak.musicbox.flags.Flags;
import com.ronak.musicbox.flags.Url_format;

import java.io.IOException;

/**
 Created by ronak on 01/23/17
 */
@Deprecated
public class AsyncStationsDownload extends AsyncTask<String, Integer, String> {


    private String data;

    @Override
    protected String doInBackground(String... params) {
        Url_format uri_format = new Url_format();
        try {
            data = DownloadContent.downloadContent(uri_format.getTopStationsXML(Flags.DEV_ID, "0",
                    params[0],
                    params[1],
                    params[2]));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

}
