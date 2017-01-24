package com.ronak.musicbox;

import com.google.android.exoplayer.ExoPlayer;

/**
 * Created by ronak on 01/23/17.
 */
public class Exo {
    private static Exo ourInstance = new Exo();
    private static ExoPlayer exoPlayer = ExoPlayer.Factory.newInstance(1);


    public static Exo getInstance() {
        if (exoPlayer != null) {
            return ourInstance;
        } else {
            return new Exo();
        }

    }

    public static ExoPlayer getPlayer() {
//	  exoPlayer.
//	  Flags.AUDIO_SESSION_ID=new PlayerControl(exoPlayer).getAudioSessionId();
        if (exoPlayer != null)
            return exoPlayer;
        else
            return ExoPlayer.Factory.newInstance(1);
    }

}
