package net.tedwong.ping;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    EditText ring;
    EditText front;
    EditText back;
    EditText gps;
    Button confirm;

    boolean permissionGranted;
    final String[] PERMISSIONS = new String[]{Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW};
    final int PERMISSIONS_CODE = 0;
    int OVERLAY_PERMISSION_REQ_CODE = 1234;

    SharedPreferences settings;
    SharedPreferences.Editor editor;
    public static final String KEYS_PREF = "KEYS";        // Just the name of the file to save key words

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init SharedPreferences
        settings = getApplicationContext().getSharedPreferences(KEYS_PREF, 0);
        editor = settings.edit();

        ring = (EditText) findViewById(R.id.etRing);
        front = (EditText) findViewById(R.id.etFront);
        back = (EditText) findViewById(R.id.etBack);
        gps = (EditText) findViewById(R.id.etGPS);
        confirm = (Button) findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor.putString("ring", ring.getText().toString());
                editor.putString("front", front.getText().toString());
                editor.putString("back", back.getText().toString());
                editor.putString("gps", gps.getText().toString());
                editor.apply();
                Toast.makeText(getApplicationContext(), "Key words saved", Toast.LENGTH_LONG).show();
            }
        });

        ring.setText(settings.getString("ring", "Pinpoint"));
        front.setText(settings.getString("front", "Pinpoint front"));
        back.setText(settings.getString("back", "Pinpoint back"));
        gps.setText(settings.getString("gps", "Pinpoint gps"));


        // Request Permissions
        permissionGranted = hasPermissions(this, PERMISSIONS);
        requestOverlay();   // For some reason, Android 22+ requires requesting this in a different fashion
        if (!hasPermissions(this, PERMISSIONS)) {
            Toast.makeText(getBaseContext(), "Permissions need to be granted", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_CODE);
        } else {    // Has ALL permissions
            Toast.makeText(getBaseContext(), "You have all permissions granted", Toast.LENGTH_LONG).show();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("THEODORE", "" + permission);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getBaseContext(), "You have accepted all permissions", Toast.LENGTH_LONG).show();
                    permissionGranted = true;

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getBaseContext(), "Accept the permissions for the app to work", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    // Need screenoverlay for taking pictures on camera discretely
    public void requestOverlay() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= 23 && requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted...
                Toast.makeText(getBaseContext(), "Not granted", Toast.LENGTH_LONG).show();
                requestOverlay();
            } else {
                Toast.makeText(getBaseContext(), "granted", Toast.LENGTH_LONG).show();
            }
        }
    }

}