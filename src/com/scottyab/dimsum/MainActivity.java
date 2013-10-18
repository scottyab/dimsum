package com.scottyab.dimsum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.appaholics.circularseekbar.CircularSeekBar;
import com.appaholics.circularseekbar.CircularSeekBar.OnSeekChangeListener;
import com.scottyab.dimsum.service.ScreenDimmingService;

public class MainActivity extends Activity {

	private CircularSeekBar circularSeekbar;
	private SharedPreferences mSharedPrefs;
	private Button saveBrightness;
	private Button intentBrightness;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSharedPrefs = getSharedPreferences(getPackageName(),
				Context.MODE_PRIVATE);

		initViws();

		// schedule the service
		ScreenDimmingService.schedulePendingIntent(getApplicationContext(),
				mSharedPrefs.getInt(Constants.PREF_KEY_FREQ, 1));
	}

	@Override
	protected void onResume() {
		updateSeekBarWithCurrentBrightness();
		super.onResume();
	}

	private void updateSeekBarWithCurrentBrightness() {
		float currentBirghtness = Utils
				.getCurrentScreenBrightness(getApplicationContext());
		circularSeekbar.setProgress(Math.round(currentBirghtness * 100));
	}

	private void initViws() {
		saveBrightness = (Button) findViewById(R.id.saveCurrentBrightess);
		saveBrightness.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int progress = circularSeekbar.getProgress();
				// value between 0-1 so dividie 100
				float brightnessValue = progress / 100;
				Utils.setScreenBrightness(brightnessValue,
						getApplicationContext());
			}
		});

		intentBrightness = (Button) findViewById(R.id.sendBrightnessIntent);
		intentBrightness.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startService(ScreenDimmingService
						.getStartingIntent(getApplicationContext()));
			}
		});

		circularSeekbar = (CircularSeekBar) findViewById(R.id.circularSeekBar1);
		circularSeekbar.setMaxProgress(100);

		final Window window = getWindow();

		circularSeekbar.setSeekBarChangeListener(new OnSeekChangeListener() {
			@Override
			public void onProgressChange(final CircularSeekBar view,
					final int newProgress) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Log.d("",
								"Progress:" + view.getProgress() + "/"
										+ view.getMaxProgress());
						float brightness = newProgress;
						final WindowManager.LayoutParams lp = window
								.getAttributes();
						lp.screenBrightness = brightness / 255.0f;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								window.setAttributes(lp);
							}
						});

					}
				}).start();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings) {
			startActivity(new Intent(getApplicationContext(),
					SettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
