package com.example.ridekeeper;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* This class will handle all Parse's Push notifications whose "action" is "CUSTOMIZED".
 * This is define in AndroidManifiest.xml ( <action android:name="CUSTOMIZED" /> )
 */
public class ParseCustomReceiver extends BroadcastReceiver{
	
	/* Sample Push 
	    {
		  "action": "CUSTOMIZED",
		  "alertLevel": "MVT",
		  "vehicleName": "XYZ ABC 2010"
		}
	 */
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			//String action = intent.getAction();
			JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			String alertLevel = json.getString("alertLevel");
			String vehicleName = json.getString("vehicleName");
				
			if (alertLevel.equalsIgnoreCase("nearby")){
				NotificationMgr.nearbyVBSAlert(context, vehicleName, App.isMainActivityRunning);
			}else if (alertLevel.equalsIgnoreCase("TLT")){
				NotificationMgr.ownerVehicleLiftTiltAlert(context, vehicleName, App.isMainActivityRunning);
			/*}else if (alertType.equalsIgnoreCase("lifted")){
				NotificationMgr.ownerVehicleLiftTiltAlert(context, App.isMainActivityRunning);*/
			}else if (alertLevel.equalsIgnoreCase("MVT")){
				NotificationMgr.ownerVehicleStolenAlert(context, vehicleName, App.isMainActivityRunning);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
}
