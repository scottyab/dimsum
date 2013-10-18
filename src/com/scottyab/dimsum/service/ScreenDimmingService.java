package com.scottyab.dimsum.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.scottyab.dimsum.Constants;
import com.scottyab.dimsum.DummyBrightnessActivity;
import com.scottyab.dimsum.MainActivity;
import com.scottyab.dimsum.R;
import com.scottyab.dimsum.Utils;

public class ScreenDimmingService extends IntentService {

	private final static float MIN_BRIGHTNESS = 0;
	private final static float MAX_BRIGHTNESS = 1;

	private SharedPreferences mSharedPrefs;

	private static final String TAG = "ScreenDimmingService";
	private static final int THRESHHOLD_REACHED_NOTIFICATION_ID = 11;
	private static final int STOP_RC = 1;

	public ScreenDimmingService() {
		super(ScreenDimmingService.class.getSimpleName());

	}

	public static Intent getStartingIntent(Context context) {
		Intent start = new Intent(context, ScreenDimmingService.class);
		return start;
	}

	public static void schedulePendingIntent(Context context, int frequency) {

		Intent intent = getStartingIntent(context);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(ALARM_SERVICE);
		// Will update the current pending intent
		PendingIntent pi = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// schedule
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + frequency
						* AlarmManager.INTERVAL_HOUR, frequency
						* AlarmManager.INTERVAL_HOUR, pi);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mSharedPrefs = getSharedPreferences(getPackageName(),
				Context.MODE_PRIVATE);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		boolean enabled = mSharedPrefs.getBoolean(Constants.PREF_KEY_ENABLED,
				true);

		if (!enabled) {
			Log.d(TAG, "brightness not altered, enabled set to false");
			stopSelf();
		}

		boolean mode = mSharedPrefs.getBoolean(Constants.PREF_KEY_MODE, true);
		int frequency = mSharedPrefs.getInt(Constants.PREF_KEY_FREQ, 1);
		float step = mSharedPrefs.getFloat(Constants.PREF_KEY_STEP, 0.05f);

		// get current screen dim level
		float currentBrightness = Utils
				.getCurrentScreenBrightness(getApplicationContext());

		// calc what the new value should be based on prefs
		float newBrightness = calcNewBrightnessValue(currentBrightness, mode,
				step);

		// save the previlously set value (encase user changes to auto)
		setPreviousBightness(currentBrightness);

		if (isNewBightnessValueInThreshold(newBrightness, mode, getThreshHold())) {
			// dim the screen
			Utils.setScreenBrightness(newBrightness, getApplicationContext());
			// kick off activity to actually apply the setting
			getApplication().startActivity(
					DummyBrightnessActivity.getStartingIntent(newBrightness,
							getApplicationContext()));
		} else {
			// send notification
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(THRESHHOLD_REACHED_NOTIFICATION_ID,
					createNotification());
		}

		// set pending intent wake up service and dim screen
		schedulePendingIntent(getApplicationContext(), frequency);
	}

	private Notification createNotification() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				getApplicationContext());

		Intent disableIntent = ScreenDimmingService
				.getStartingIntent(getApplicationContext());
		PendingIntent disablePendingIntent = PendingIntent.getService(this,
				STOP_RC, disableIntent, 0);

		Intent openApp = new Intent(getApplicationContext(), MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				openApp, 0);

		final boolean mode = mSharedPrefs.getBoolean(Constants.PREF_KEY_MODE,
				true);

		String contentText = getString(R.string.notification_dimming_complete);
		String contentTitle = getString(R.string.notification_dimming_complete_title);
		if (!mode) {
			contentTitle = getString(R.string.notification_brightening_complete_title);
			contentText = getString(R.string.notification_brightening_complete);
		}

		builder.setContentTitle(contentTitle)
				.setContentText(contentText)
				.addAction(0, getString(R.string.notification_action_disable),
						disablePendingIntent).setOngoing(true)
				.setContentIntent(pendingIntent);

		// TODO reset

		return builder.build();
	}

	private boolean isNewBightnessValueInThreshold(float newBrightness,
			boolean mode, float threshHold) {
		// is dimming and new value too dim
		if (mode && newBrightness < getThreshHold()) {
			return false;
			// is brightening and new value too bright
		} else if (!mode && newBrightness > getThreshHold()) {
			return false;
		}

		// else it;s withIn the Threadhold
		return true;
	}

	private float calcNewBrightnessValue(float currentBrightness, boolean mode,
			float step) {
		float newBrightness = MIN_BRIGHTNESS;
		if (mode) {
			newBrightness = currentBrightness - step;
		} else {
			newBrightness = currentBrightness + step;
		}
		return newBrightness;
	}

	private float getPreviousBightness() {
		return mSharedPrefs.getFloat(
				Constants.PREF_KEY_PREVOUIS_BRIGHTNESS_VALUE, -1f);
	}

	private float getThreshHold() {
		return mSharedPrefs.getFloat(
				Constants.PREF_KEY_THREASHOLD_BRIGHTNESS_VALUE, -1f);
	}

	private void setPreviousBightness(float previousBrightness) {
		Editor editor = mSharedPrefs.edit();
		editor.putFloat(Constants.PREF_KEY_PREVOUIS_BRIGHTNESS_VALUE,
				previousBrightness);
		editor.commit();
	}

}
