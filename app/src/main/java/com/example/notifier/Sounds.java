package com.example.notifier;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

class Sounds
{
    public static final String LOG_TAG = "Sounds";
    public static void startPlaying(Context context, int rid) {
//        MediaPlayer mediaPlayer = MediaPlayer.create(context, rid);
//        mediaPlayer.start();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, rid);
                        mediaPlayer.start();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "startPlaying(...) PROBLEMS " + e.getMessage());
                    }
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (Exception e) {
            Log.e(LOG_TAG, "t.join problems " + e.getMessage());
        }
    }
}