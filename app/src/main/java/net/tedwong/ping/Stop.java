package net.tedwong.ping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

public class Stop extends Activity {
    private static Stop inst;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop);

        inst = this;
        mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.asd);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        startSound();
        startVibrate();
    }

    public Stop instance() {
        return inst;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            /*If sound is playing, stops*/
            stopSound();
            stopVibrate();
            finish();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void startSound() {
        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Set volume to max
        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);

        // Play notification sound
        mediaPlayer.setLooping(true);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    public void stopSound() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void startVibrate() {
        long pattern[] = {0, 100, 200, 300, 400};
        vibrator.vibrate(pattern, 0);
    }

    public void stopVibrate() {
        vibrator.cancel();
    }
}
