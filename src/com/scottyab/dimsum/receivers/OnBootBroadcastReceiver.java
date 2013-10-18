package com.scottyab.dimsum.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.scottyab.dimsum.service.ScreenDimmingService;

public class OnBootBroadcastReceiver extends BroadcastReceiver {

	static final String TAG = "StartupBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d(TAG, "onReceive: phone rebooted -> start alarm and listener");

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			context.startService(ScreenDimmingService
					.getStartingIntent(context));
		}
	}
}