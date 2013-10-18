package com.scottyab.dimsum;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class Utils {

	public static float getCurrentScreenBrightness(Context context) {
		try {
			float curBrightnessValue = android.provider.Settings.System.getInt(
					context.getContentResolver(),
					android.provider.Settings.System.SCREEN_BRIGHTNESS);
			return curBrightnessValue;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();

		}
		return -1;
	}

	// SCREEN_BRIGHTNESS 0-255
	public static void setScreenBrightness(float newBrightnessLevel,
			Context context) {
		// This is important. In the next line 'brightness'
		// should be a float number between 0.0 and 1.0
		int brightnessInt = (int) (newBrightnessLevel * 255);

		// Check that the brightness is not 0, which would effectively
		// switch off the screen, and we don't want that:
		if (brightnessInt < 1) {
			brightnessInt = 1;
		}

		// Set systemwide brightness setting.
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, brightnessInt);

	}

}
