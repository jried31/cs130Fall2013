package com.example.ridekeeper;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.settings);
	}
	
	@Override
	public void onDetach() {
		Preferences.loadSettingsFromSharedPref(getActivity());
		super.onDetach();
	}
}
