package com.example.ridekeeper;

import java.util.Calendar;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationMgr {
	
	public static LocationManager locationManager;
	public static Location myLocation;
	
	//Used as a callback for updatetLocation_inBackground()
	interface GetLocCallback{
		void done();;
	}
	
	public static void initialize(Context context){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        myLocation = LocationMgr.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	public static void getLastGoodLoc(){
		myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (myLocation == null){
			myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
	}
	
	public static void updatetLocation_inBackground(Context context, final GetLocCallback callback){
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				myLocation = location;
				callback.done();
				locationManager.removeUpdates(this);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
	public static void updatetLocation_Blocked(Context context){
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				myLocation = location;
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		//10 seconds timeout for GPS lock
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
		int counter = 0;
		
		//Block until we have a GPS lock or timeout
		while (myLocation==null || myLocation.getTime() < Calendar.getInstance().getTimeInMillis() - 2*60*1000){
			myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			if (counter >= 10) break; //Timeout = 10 seconds
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			counter++;
		}
		
		locationManager.removeUpdates(locationListener);
	}
	
	

	
	
}
