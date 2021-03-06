/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ridekeeper;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseUser;

public class MainActivity extends Activity implements LocationListener {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mDrawerMenuTitles;

	private enum SelectedFrag{
		STOLENVEHICLE, MYPROFILE, MYVEHICLES, SETTINGS
	}
	private SelectedFrag selectedFrag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		App.isMainActivityRunning = true;
		App.bReceiver.setRepeatingAlarm(this);

		mTitle = mDrawerTitle = getTitle();
		mDrawerMenuTitles = getResources().getStringArray(R.array.drawer_menu_title_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mDrawerMenuTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
				) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			
			if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
				selectItem(0); //Select VBS List Fragment as default if user is authenticated
			}else{
				selectItem(1); //Otherwise, Select My Profile Fragment so that user can login
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		//menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		menu.findItem(R.id.action_addvehicle).setVisible(false);
		menu.findItem(R.id.action_refreshvbslist).setVisible(false);
		
		if (selectedFrag == SelectedFrag.MYVEHICLES){
			//menu.findItem(R.id.action_websearch).setVisible(false);
			menu.findItem(R.id.action_addvehicle).setVisible(true & !drawerOpen);
		}else if (selectedFrag == SelectedFrag.STOLENVEHICLE) {
			menu.findItem(R.id.action_refreshvbslist).setVisible(true & !drawerOpen);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch(item.getItemId()) {
		/*
		case R.id.action_websearch:
			// create intent to perform web search for this planet
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			// catch event that there's no activity to handle intent
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
			}
			return true;
		*/
		case R.id.action_refreshvbslist:
			StolenVehicleListFragment.refreshList();
			return true;
		
		case R.id.action_addvehicle:
			EditVehicleFragment.addVehicle(getFragmentManager());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments

		Fragment fragment=null;

		switch(position){
		case DBGlobals.VBS_LIST:
			fragment = new StolenVehicleListFragment();
			selectedFrag = SelectedFrag.STOLENVEHICLE;
			break;
		case DBGlobals.MY_PROFILE:
			fragment = new MyProfileFragment();
			selectedFrag = SelectedFrag.MYPROFILE;
			break;
		case DBGlobals.MY_VEHICLE:
			fragment = new MyVehicleListFragment();
			selectedFrag = SelectedFrag.MYVEHICLES;
			break;
		case DBGlobals.SETTINGS:
			fragment = new SettingsFragment();
			selectedFrag = SelectedFrag.SETTINGS;
			break;
		default:
			fragment = new MyProfileFragment();
		}

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override()
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();

		//Stop alarm tone and vibration
		NotificationMgr.stopAlarmTone();
		NotificationMgr.stopVibration();
		
		//Start updating phone's location
		LocationMgr.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}


	@Override
	protected void onPause() {
		//stop updating phone's location
		LocationMgr.locationManager.removeUpdates(this);

		App.isMainActivityRunning = false;

		super.onPause();
	}

	@Override
	public void onLocationChanged(Location location) {
		//Toast.makeText(this, "Location updated!", Toast.LENGTH_SHORT).show();
		LocationMgr.myLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public void test(View v){
		Toast.makeText(getApplicationContext(), "START", Toast.LENGTH_SHORT).show();
		
		LocationMgr.updatetLocation_inBackground(this, new LocationMgr.GetLocCallback() {
			@Override
			public void done() {
				Toast.makeText(getApplicationContext(), "GOT LOC", Toast.LENGTH_SHORT).show();
			}
		});
		
	}
}