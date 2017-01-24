package com.ronak.musicbox.model;

import android.util.Log;

import java.util.UUID;

/**
 * Created by ronak on 01/23/17.
 */

public class StationAddedManually extends Station {
    private static final String TAG = "StatoiinAddedManually";
    private String urlstation;

    public StationAddedManually() {
        super.setType(Station.TYPE_MANUALLY_ADDED);
    }

    @Override
    public int getType() {
        return Station.TYPE_MANUALLY_ADDED;
    }

    public String getUrlstation() {
        return urlstation;
    }

    public void setUrlstation(String urlstation) {
        this.urlstation = urlstation;
    }

    public long save(String name) {
        Log.d(TAG, name + " station saved.");
        if (getStationId() == null || getStationId().isEmpty()) {
            String id = UUID.randomUUID().toString();
            setStationId(id);
            super.setStationId(id);
            return super.save();
        } else {
            return super.save();
        }
    }

    public boolean isValidStation() {

        return this.getUrlstation() != null && !this.getUrlstation().isEmpty();
    }
}
