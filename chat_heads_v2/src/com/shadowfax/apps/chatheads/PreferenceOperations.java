package com.shadowfax.apps.chatheads;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceOperations {
	Context context;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;

	public PreferenceOperations(Context context) {
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = preferences.edit();
	}

	public void initAppBasedOnPref() {
		int no_times_app_used = preferences.getInt("no_times_app_used", -1);
		if (no_times_app_used == -1) {
			AlertDialog.Builder alert = new AlertDialog.Builder(context);

			alert.setTitle("Important");
			String msg = "Thank you for downloading iRingtone.\n\n"
					+ "This is a free version of the app and hence ads will be displayed.\n";
			alert.setMessage(msg);
			alert.setNeutralButton("close",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

						}
					});

			alert.show();
			increaseAppUsedPerf();
		} else {
			//Do Nothing
		}

	}
	public void increaseAppUsedPerf() {
		int no_times_app_used = preferences.getInt("no_times_app_used", -1);
		editor.putInt("no_times_app_used", ++no_times_app_used);
		editor.commit();
	}

	public int getNumberTimesAppUsedPref() {
		int no_times_app_used = preferences.getInt("no_times_app_used", -1);
		return no_times_app_used;
	}

	public boolean getChatHeadsStatePref() {
		boolean cleverToneState = preferences.getBoolean(
				"clevertone_state_pref", false);
		return cleverToneState;
	}

	public void storeCleverStatePref(boolean cleverToneState) {
		editor.putBoolean("clevertone_state_pref", cleverToneState);
		editor.commit();
	}

}
