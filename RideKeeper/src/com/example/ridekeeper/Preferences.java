package com.example.ridekeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
	public static boolean 	other_beep,
							other_alert,
							other_shortvibration,
							other_longvibration,

							owner_stolen_beep,
							owner_stolen_alert,
							owner_stolen_shortvibration,
							owner_stolen_longvibration,

							owner_lt_beep,
							owner_lt_alert,
							owner_lt_shortvibration,
							owner_lt_longvibration;

	public static void loadSettingsFromSharedPref(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		other_beep = pref.getBoolean("other_beep", true);
		other_alert = pref.getBoolean("other_alert", true);
		other_shortvibration = pref.getBoolean("other_shortvibration", true);
		other_longvibration = pref.getBoolean("other_longvibration", true);

		owner_stolen_beep= pref.getBoolean("owner_stolen_beep", true);
		owner_stolen_alert= pref.getBoolean("owner_stolen_alert", true);
		owner_stolen_shortvibration = pref.getBoolean("owner_stolen_shortvibration", true);
		owner_stolen_longvibration = pref.getBoolean("owner_stolen_longvibration", true);

		owner_lt_beep = pref.getBoolean("owner_lt_beep", true);
		owner_lt_alert = pref.getBoolean("owner_lt_alert", true);
		owner_lt_shortvibration = pref.getBoolean("owner_lt_shortvibration", true);
		owner_lt_longvibration = pref.getBoolean("owner_lt_longvibration", true);
	}
}
