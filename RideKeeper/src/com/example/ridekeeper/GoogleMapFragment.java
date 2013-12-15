package com.example.ridekeeper;

import java.util.List;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class GoogleMapFragment extends DialogFragment implements LocationListener {
	private GoogleMap mMap;
	private MapView mMapView;
	private Bundle mBundle;
	
	private String UIDtoTrack = null;	//VBS UID to be tracked, null for all VBS
	private Marker markerVehicle;
	
    public Fragment newInstance(Context context) {
    	GoogleMapFragment f = new GoogleMapFragment();
    	
    	return f;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_googlemap, container, false);
        
        try{
        	MapsInitializer.initialize(getActivity());
        }catch (GooglePlayServicesNotAvailableException e){
        	
        }
        
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        
    	if (mMap==null){
    		mMap = ((MapView) view.findViewById(R.id.map)).getMap();
    	}
		mMap.setMyLocationEnabled(true);
  		//mMap.setOnMarkerClickListener(this);
  		mMap.getUiSettings().setCompassEnabled(true);
  		mMap.getUiSettings().setZoomControlsEnabled(true);
  		
  		
  		//Move camera to current phone's location
  		LocationMgr.getLastGoodLoc();
  		
  		if (LocationMgr.myLocation!=null){
  			LatLng myLatLng = new LatLng( LocationMgr.myLocation.getLatitude(), LocationMgr.myLocation.getLongitude() );
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng , 15f));
  		}
  		
  		//Start dynamically showing marker on the map
  		MarkerOptions markerOption = new MarkerOptions();
  		markerOption.position(new LatLng(0, 0)).visible(false);
  		markerVehicle = mMap.addMarker(markerOption);
  		
        mHandler.postDelayed(runQueryVBS, 1000);
        
        return view;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	mBundle = savedInstanceState;
    	
    	//Load  UID argument for tracking
    	if (getArguments()!=null && getArguments().containsKey("UID")){
        	UIDtoTrack = getArguments().getString("UID");
        	//Toast.makeText(getActivity(), UIDtoTrack, Toast.LENGTH_SHORT).show();
    	}else{
    		Toast.makeText(getActivity(), "No vehicle UID provided to track.", Toast.LENGTH_SHORT).show();
    		UIDtoTrack = "";
    	}
    	
    	//setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    	setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Light);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	mMapView.onResume();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mMapView.onPause();
    }
    
    @Override
    public void onDestroy() {
    	mHandler.removeCallbacksAndMessages(null); //Cancel dynamic update of the map
    	mMapView.onDestroy();
    	super.onDestroy();
    }

	@Override
	public void onLocationChanged(Location location) {
		//Update phone's GPS location
		LocationMgr.myLocation = location;
	}
	
	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}

	
	//Callback when query gets result from Parse
	private FindCallback<ParseObject> queryVehicleCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			
			if (e== null){ // no error
				if (objects.size()>0){
					ParseGeoPoint p =  objects.get(0).getParseGeoPoint("pos");
					markerVehicle.setPosition( new LatLng(p.getLatitude(), p.getLongitude()) );
					markerVehicle.setTitle(	objects.get(0).getString("make") + " " +
											objects.get(0).getString("model") + " " +
											objects.get(0).getNumber("year").toString() + " "
											);
					markerVehicle.setVisible(true);
				}else{ //Can't find vehicle
	
				}
				
				mHandler.postDelayed(runQueryVBS, DBGlobals.vehiclePosUpdateInGMapRate); //Refresh rate = 3 seconds if no error

			}else{ //error occurred when query to Parse
				Toast.makeText(getActivity(), "Error querying Parse server", Toast.LENGTH_SHORT).show();
				
				mHandler.postDelayed(runQueryVBS, 15000);  //Refresh rate = 15 seconds if error occurs
			}
		}
	};
	

	//Dynamically update vehicle position on map
	final Handler mHandler = new Handler();
    final Runnable runQueryVBS = new Runnable() {
    	@Override
		public void run() {
			// TODO Turn Internet connection on if needed
    		
			ParseQuery<ParseObject> query = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL);
			query.whereEqualTo("objectId", UIDtoTrack);
			query.findInBackground(queryVehicleCallback);
		}
	};
	
}










