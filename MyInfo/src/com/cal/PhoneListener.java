package com.cal;

import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;

public class PhoneListener extends PhoneStateListener
{
    private Context context;
    private static final String TAG = "KAL";

    public PhoneListener(Context c) {
        Log.i(TAG, "PhoneListener constructor");
        context = c;
    }

    public void onCallStateChanged (int state, String incomingNumber)
    {
        Log.d(TAG, "PhoneListener::onCallStateChanged state:" + state + " incomingNumber:" + incomingNumber);

        switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
            Log.d(TAG, "CALL_STATE_IDLE, stoping recording");
            Boolean stopped = context.stopService(new Intent(context, MyInfoService.class));
            Log.i(TAG, "stopService for MyInfoService returned " + stopped);
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            Log.d(TAG, "CALL_STATE_RINGING");
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            Log.d(TAG, "CALL_STATE_OFFHOOK starting recording");
            Intent callIntent = new Intent(context, MyInfoService.class);
            ComponentName name = context.startService(callIntent);
            if (null == name) {
                Log.e(TAG, "startService for MyInfoService returned null ComponentName");
            } else {
                Log.i(TAG, "startService returned " + name.flattenToString());
            }
            break;
        }
    }
}
