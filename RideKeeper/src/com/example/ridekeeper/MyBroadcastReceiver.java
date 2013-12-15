package com.example.ridekeeper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;


public class MyBroadcastReceiver extends BroadcastReceiver{
	
	@SuppressLint("Wakelock")
	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RideKeeper");
		
		wl.acquire();
		ParseFunctions.updateLocToParse(context); //Periodically update phone's location to Parse server
		wl.release();
	}
	
	public void setRepeatingAlarm(Context context){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, MyBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, DBGlobals.repeatingAlarmRate, pi); //do every 1 minutes
		//Toast.makeText(context, "Alarm started", Toast.LENGTH_SHORT).show();
	}

	public void cancelAlarm(Context context){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, MyBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
		//Toast.makeText(context, "Alarm canceled", Toast.LENGTH_SHORT).show();
	}
	
	/* This functions will not be used.
	 * Instead of doing routineCheck, the client app should wait for
	 * push notification from Parse server.
	*/

	/*
	private void routineCheck(Context context){	
		// TODO Turn Internet connection on if needed
		
		//Get phone's GPS location
		//Toast.makeText(context, "Getting GPS", Toast.LENGTH_SHORT).show();
		HelperFuncs.updatetLocation_Blocked(context);
		
		//Toast.makeText(context, "Querying VBS", Toast.LENGTH_SHORT).show();
		//Query Parse server for nearby VBS
		
		if (HelperFuncs.myLocation!=null){
			List<ParseObject> vbsList = HelperFuncs.queryForVBS_Blocked(HelperFuncs.myLocation.getLatitude(),
					HelperFuncs.myLocation.getLongitude(),
					DBGlobals.searchRadius); //search within this miles radius
			
			if (vbsList!=null && vbsList.size()>0){ //There is at least one vehicle being stolen nearby
				HelperFuncs.StartVibration();
				HelperFuncs.playAlarmTone();
				HelperFuncs.CreateNotif(context,
				Integer.toString(vbsList.size()) + " vehicle(s) being stolen nearby",
				"Click for more info");
				}else{
				HelperFuncs.CreateNotif(context, "No Vehicle being stolen nearby", "");
			}
		}else{
			HelperFuncs.CreateNotif(context, "Can't get phone's GPS location", "");
		}
	}
	*/
	
	/*
	//Update phone's location to parse server
	private void updateLocToParse(Context context){
		//Toast.makeText(context, "Update loc to Parse", Toast.LENGTH_SHORT).show();
		//HelperFuncs.updatetLocation_Blocked(context);
		
		HelperFuncs.updatetLocation_inBackground(context, new HelperFuncs.GetLocCallback() {
			@Override
			public void done() {
				if (HelperFuncs.myLocation != null){
					ParseGeoPoint myGeo = new ParseGeoPoint( HelperFuncs.myLocation.getLatitude(),
															HelperFuncs.myLocation.getLongitude() );
					ParseInstallation.getCurrentInstallation().put("GeoPoint", myGeo);
					ParseInstallation.getCurrentInstallation().saveInBackground();
				}
			}
		});
	}
	*/

}
