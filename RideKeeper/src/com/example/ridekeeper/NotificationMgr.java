package com.example.ridekeeper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class NotificationMgr {
	private static Ringtone alarmTone;
	private static Vibrator myVibrator;
	private static long[] vibrationPattern = {0, 200, 500, 100, 0, 0, 0, 0};
	private static MediaPlayer mediaPlayer;
	

	private static void initialVibrator(Context context){
		myVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}

	private static void initialAlarmTone(Context context){
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		alarmTone = RingtoneManager.getRingtone(context, notification);
	}
	
	public static void initialize(Context context){
        initialAlarmTone(context);
        initialVibrator(context);

        mediaPlayer = MediaPlayer.create(context,R.raw.beep3);
	}
	
	public static void createAndroidNotification(Context context, String title, String contentText){
		NotificationCompat.Builder notifBuilder =
				new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setContentText(contentText)
				.setAutoCancel(true);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(new Intent(context, MainActivity.class));
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		notifBuilder.setContentIntent(resultPendingIntent);
		NotificationManager notifManager =
			    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(0, notifBuilder.build());
	}
	
	public static void vibrationLong(){
		myVibrator.vibrate(vibrationPattern, 0);
	}
	
	public static void vibrationShort(){
		myVibrator.vibrate(300);
	}
	
	public static void stopVibration(){
		myVibrator.cancel();
	}

	
	
	public static void playAlarmTone(){
		if (alarmTone!=null)
			alarmTone.play();
	}
	
	public static void stopAlarmTone(){
		if (alarmTone!=null && alarmTone.isPlaying())
			alarmTone.stop();
	}
	
	public static void playBeep(){
		mediaPlayer.start();
	}
	
	public static void nearbyVBSAlert(Context context, String vehicleName, boolean isAppRunning){
		createAndroidNotification(context, "A " + vehicleName + " was stolen nearby.", "Click for more info");
		
		if (Preferences.other_beep)
			playBeep();
		if (Preferences.other_alert && !isAppRunning)
			playAlarmTone();
		if (Preferences.other_shortvibration || isAppRunning)
			vibrationShort();
		if (Preferences.other_longvibration && !isAppRunning)
			vibrationLong();
	}
	
	public static void ownerVehicleLiftTiltAlert(Context context, String vehicleName, boolean isAppRunning){
		createAndroidNotification(context, "Your " + vehicleName + " was tilted/lifted!!", "");
		
		if (Preferences.owner_lt_beep)
			playBeep();
		if (Preferences.owner_lt_alert && !isAppRunning)
			playAlarmTone();
		if (Preferences.owner_lt_shortvibration || isAppRunning)
			vibrationShort();
		if (Preferences.owner_lt_longvibration && !isAppRunning)
			vibrationLong();
	}
	
	public static void ownerVehicleStolenAlert(Context context, String vehicleName, boolean isAppRunning){
		createAndroidNotification(context, "Your " + vehicleName +" was STOLEN!!", "");
		
		if (Preferences.owner_stolen_beep)
			playBeep();
		if (Preferences.owner_stolen_alert && !isAppRunning)
			playAlarmTone();
		if (Preferences.owner_stolen_shortvibration || isAppRunning)
			vibrationShort();
		if (Preferences.owner_stolen_longvibration && !isAppRunning)
			vibrationLong();
	}
}
