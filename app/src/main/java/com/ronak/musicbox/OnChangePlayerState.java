package com.ronak.musicbox;

import com.google.android.exoplayer.ExoPlaybackException;

/**
 * Created by ronak on 01/23/17
 */
interface OnChangePlayerState {
    void onStateChanged(boolean playWhenReady, int playbackState);
    void onPlayerError(ExoPlaybackException error);
}
