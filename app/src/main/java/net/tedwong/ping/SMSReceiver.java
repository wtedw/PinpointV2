package net.tedwong.ping;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String address = "";
        String strMessage = "";
        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = myBundle.getString("format");
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                address = messages[i].getOriginatingAddress();
                strMessage += messages[i].getMessageBody();
            }

            checkMessage(context, address, strMessage);
        }
    }

    private void checkMessage(Context context, String address, String strMessage) {
        SharedPreferences keyWords = context.getSharedPreferences(MainActivity.KEYS_PREF, 0);

        if (strMessage.equals(keyWords.getString("ring", "Pinpoint"))) {
            Intent i = new Intent(context, Stop.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        if (strMessage.equals(keyWords.getString("front", "Pinpoint front"))) {
            PhotoTakingService.instance(context).takeFrontPhoto(address);
        }
        if (strMessage.equals(keyWords.getString("back", "Pinpoint back"))) {
            PhotoTakingService.instance(context).takeBackPhoto(address);
        }
        if (strMessage.equals(keyWords.getString("gps", "Pinpoint gps"))) {
            if (!getGPS(context, address)) {
                sendSMS(address, "GPS and Network not detected");
            } else {
                // It has sent GPS coords
            }
        }
    }

    // Attempt to get GPS location (if GPS is turned on)
    // If not found, say sorry, Android won't let you turn on the phone's GPS programatically anymore
    public boolean getGPS(final Context context, final String address) {
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                sendSMS(address, "" + location.getLatitude() + ", " + location.getLongitude());
            }
        };
        MyLocation myLocation = new MyLocation();
        return myLocation.getLocation(context, locationResult);
    }

    private void sendSMS(String phoneNumber, String body) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, body, null, null);
    }
}