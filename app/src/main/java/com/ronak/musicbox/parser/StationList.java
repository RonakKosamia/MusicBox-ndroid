package com.ronak.musicbox.parser;

import com.ronak.musicbox.model.Station;

import java.util.ArrayList;

/**
 * Created by ronak on 01/23/17.
 */
public class StationList {
   private ArrayList<Station> arrayListStations;
   private TuneIn tuneIn;

   public StationList() {
   }

   public ArrayList<Station> getArrayListStations() {
	  return arrayListStations;
   }

   public void setArrayListStations(ArrayList<Station> arrayListStations) {
	  this.arrayListStations = arrayListStations;
   }

   public TuneIn getTuneIn() {
	  return tuneIn;
   }

   public void setTuneIn(TuneIn tuneIn) {
	  this.tuneIn = tuneIn;
   }
}
