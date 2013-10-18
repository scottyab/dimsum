package com.scottyab.dimsum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

public class DummyBrightnessActivity extends Activity {

	private static final int DELAYED_MESSAGE = 1;
	private static final String EXTRA_BRIGHTNESS = "EXTRA_BRIGHTNESS";

	private Handler handler;

	public static Intent getStartingIntent(float brightness, Context context) {
		Intent dummyBrightIntent = new Intent(context,
				DummyBrightnessActivity.class);
		dummyBrightIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		dummyBrightIntent.putExtra("brightness value", brightness);
		return dummyBrightIntent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == DELAYED_MESSAGE) {
					DummyBrightnessActivity.this.finish();
				}
				super.handleMessage(msg);
			}
		};
		Intent brightnessIntent = this.getIntent();
		float brightness = brightnessIntent
				.getFloatExtra("brightness value", 0);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = brightness;
		getWindow().setAttributes(lp);

		Message message = handler.obtainMessage(DELAYED_MESSAGE);
		// this next line is very important, you need to finish your activity
		// with slight delay
		handler.sendMessageDelayed(message, 1000);
	}

}
