package com.scottyab.dimsum.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Reciever that listens for power releated Broadcasts
 * 
 * @author scottab
 * 
 */
public class PowerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
			// Do something when power connected
		} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
			// Do something when power disconnected
		}

	}

}
