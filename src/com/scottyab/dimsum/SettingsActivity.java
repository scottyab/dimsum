package com.scottyab.dimsum;

import java.lang.reflect.Type;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	private static final String TAG = "SettingsActivity";

	private String version;

	private SharedPreferences mSharedPrefs;

	private String[] freqLabels;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		freqLabels = getResources().getStringArray(R.array.freqs_labels);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			// TODO: if the previous activity on the stack isn't a
			// launch it.
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void setupSimplePreferencesScreen() {
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		mSharedPrefs = getSharedPreferences(getPackageName(),
				Context.MODE_PRIVATE);

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.settings);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToFreqValue(findPreference(Constants.PREF_KEY_FREQ));

		version = getAppVersion();
		findPreference("pref_version").setTitle(version);
		findPreference("pref_feedback").setIntent(createFeedbackEmailIntent());

	}

	private String getAppVersion() {
		String versionName = null;
		int versionCode = -1;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionName = pInfo.versionName;
			// not used
			// versionCode = pInfo.versionCode;
		} catch (NameNotFoundException ex) {
			Log.w(TAG, "Could not get app version info for pref page", ex);
			versionName = null;
		}

		return getString(R.string.settings_about_version, versionName);
	}

	private Intent createFeedbackEmailIntent() {
		final Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("text/plain");
		emailIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, Constants.DEV_FEEDBACK_EMAIL);
		emailIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { Constants.DEV_FEEDBACK_EMAIL });
		emailIntent.putExtra(
				Intent.EXTRA_SUBJECT,
				getString(R.string.settings_about_feedback_email_subject,
						getString(R.string.app_name), version));

		return Intent.createChooser(emailIntent,
				getString(R.string.settings_feedback_email_chooser));

	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			if (value instanceof String) {
				String stringValue = value.toString();

				if (preference instanceof ListPreference) {
					// For list preferences, look up the correct display value
					// in
					// the preference's 'entries' list.
					ListPreference listPreference = (ListPreference) preference;
					int index = listPreference.findIndexOfValue(stringValue);

					// Set the summary to reflect the new value.
					preference.setSummary(index >= 0 ? listPreference
							.getEntries()[index] : null);

				} else {
					// For all other preferences, set the summary to the value's
					// simple string representation.
					preference.setSummary(stringValue);
				}
			} else if (value instanceof Integer || value instanceof Long) {
				preference.setSummary(value + "");
			} else if (value instanceof Boolean) {
				Boolean boolValue = (Boolean) value;
				preference.setSummary(boolValue.toString());
			}

			return true;
		}
	};

	private Preference.OnPreferenceChangeListener bindFreqPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			if (value instanceof String) {
				if (!TextUtils.isEmpty((String) value)) {
					// int pos = Integer.valueOf((String) value);
					preference.setSummary("Every " + (String) value + " hrs");
				}
			}
			return true;
		}
	};

	private void bindPreferenceSummaryToFreqValue(final Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(bindFreqPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		bindFreqPreferenceSummaryToValueListener.onPreferenceChange(preference,
				mSharedPrefs.getString(preference.getKey(),
						getString(R.string.settings_freq_default)));
	}

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * Note: only works of pref values that can be cast to Strings
	 * 
	 * @param type
	 *            TODO
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private void bindPreferenceSummaryToValue(final Preference preference,
			Type type) {

		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		if (type == String.class) {
			// Trigger the listener immediately with the preference's
			// current value.
			sBindPreferenceSummaryToValueListener
					.onPreferenceChange(preference,
							mSharedPrefs.getString(preference.getKey(), ""));
		} else if (type == Integer.class) {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference, mSharedPrefs.getInt(preference.getKey(), -1));
		} else if (type == Long.class) {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference, mSharedPrefs.getLong(preference.getKey(), -1));
		} else if (type == Boolean.class) {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					mSharedPrefs.getBoolean(preference.getKey(), false));
		}

	}
}
