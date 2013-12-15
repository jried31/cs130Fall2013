package com.example.ridekeeper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import android.R.array;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ParseFunctions {
		//Query for any vehicle in the list can we're allow to see
		/* This one is an old function. use the inBackground mode 
		public static List<ParseObject> queryParseForStolenVehicle_Blocked(double lat, double lng, double withInMiles){
			ParseQuery<ParseObject> query = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL); //Query the VBS table

			ParseGeoPoint myPoint = new ParseGeoPoint(lat, lng);

			//query.whereWithinMiles("pos", myPoint, withInMiles);
			query.whereEqualTo("stolen", true); 
			
			try {
				List<ParseObject> results = query.find();
				return results;
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
			return null;
		}*/
		
		public static void queryForStolenVehicle_InBackground(double lat, double lng, double withInMiles, FindCallback<ParseObject> callback){
			ParseQuery<ParseObject> query = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL); //Query the VBS table

			//Constraints: Find any VBS within 'withInMiles' miles of given lat, lng
			//query.whereWithinMiles("pos", myPoint, withInMiles);
			query.whereEqualTo("stolen", true); //FIX THIS: query only the vehicles that we're allow to see
			
			query.findInBackground(callback);
		}
		
		public static void queryForVehicleInMyChatRoom_InBackground(double lat, double lng, double withInMiles, final FindCallback<ParseObject> callback){
			ParseQuery<ParseObject> queryChatRoom = ParseQuery.getQuery(DBGlobals.PARSE_CHATROOM_TBL); //Query the Chatroom table
			queryChatRoom.whereEqualTo("members", ParseUser.getCurrentUser().getObjectId());

			queryChatRoom.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> objects, ParseException e) {
					if (e==null && !objects.isEmpty()){
						ArrayList<String> VID = new ArrayList<String>();
						
						for (int i = 0; i < objects.size(); i++){
							VID.add(objects.get(i).getString("vehicleId"));
							Log.d("RIDEKEEPER", "VID: " + objects.get(i).getString("vehicleId"));
						}
						
						ParseQuery<ParseObject> queryVehicle = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL);
						queryVehicle.whereContainedIn("objectId", VID);
						
						queryVehicle.findInBackground(callback);
					}else{
						callback.done(objects, e);
					}
				}
				
			});
		}
		
		public static void queryForChatPhoto(String objectId, GetCallback<ParseChatRoomPhoto> callback){
			ParseQuery<ParseChatRoomPhoto> query = ParseQuery.getQuery(DBGlobals.PARSE_CHATROOMPHOTO_TBL); //Query the VBS table
			query.getInBackground(objectId, callback);
		}
		
		//Update phone's location to parse server
		public static void updateLocToParse(Context context){
			//Toast.makeText(context, "Update loc to Parse", Toast.LENGTH_SHORT).show();
			//HelperFuncs.updatetLocation_Blocked(context);
			
			LocationMgr.updatetLocation_inBackground(context, new LocationMgr.GetLocCallback() {
				@Override
				public void done() {
					if (LocationMgr.myLocation != null){
						ParseGeoPoint myGeo = new ParseGeoPoint( LocationMgr.myLocation.getLatitude(),
								LocationMgr.myLocation.getLongitude() );
						ParseInstallation.getCurrentInstallation().put("GeoPoint", myGeo);
						ParseInstallation.getCurrentInstallation().saveInBackground();
					}
				}
			});
		}
		
		//Update the ownerId field in Installation table in Parse
		//Used when user log in or phone reboot
		public static void updateOwnerIdInInstallation(){
			if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
				ParseInstallation.getCurrentInstallation().put(DBGlobals.PARSE_INSTL_OWERID, ParseUser.getCurrentUser().getObjectId());
				ParseInstallation.getCurrentInstallation().saveInBackground();
			}
		}
		
		//Remove the ownerId field in Installation table in Parse
		//Used when user log out
		public static void removeOwnerIdInInstallation(){
			if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
				ParseInstallation.getCurrentInstallation().put(DBGlobals.PARSE_INSTL_OWERID, "");
				ParseInstallation.getCurrentInstallation().saveInBackground();
			}
		}
		
		public static void postToParse(){
			ParseObject VBS = new ParseObject(DBGlobals.PARSE_VEHICLE_TBL);
			VBS.put("lat", 55.442323);
			VBS.put("lng", -77.293853);
			VBS.saveInBackground();
		}
}
