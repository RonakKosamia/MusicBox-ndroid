package com.ronak.musicbox.fragment;

import com.ronak.musicbox.model.Station;
import com.ronak.musicbox.model.StationAddedManually;

/**
 * Created by ronak on 01/23/17.
 */
public interface FavouriteClickCallbacks {
    public void favouriteAdded(Station station, int position);

    public void favouriteRemoved(Station station, int position);

    public void favrouriteDeleted(StationAddedManually manually, int adapterPosition);
}
