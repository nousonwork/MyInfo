package com.cal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;

public class MyInfoBroadcastReceiver extends BroadcastReceiver
{
	private static final String TAG = "KAL";
	
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "MyInfoBroadcastReceiver::onReceive got Intent: " + intent.toString());
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String numberToCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG, "MyInfoBroadcastReceiver intent has EXTRA_PHONE_NUMBER: " + numberToCall);
        }

        PhoneListener phoneListener = new PhoneListener(context);
        TelephonyManager telephony = (TelephonyManager)
            context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        Log.d(TAG, "PhoneStateReceiver::onReceive "+"set PhoneStateListener");
    }
    
    
}
