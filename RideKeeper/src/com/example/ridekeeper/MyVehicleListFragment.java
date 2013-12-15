package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MyVehicleListFragment extends ListFragment {
	public static ParseVehicleArrayAdapter myVehicleAdapter;
	private static Context myContext;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		myContext = getActivity();
		myVehicleAdapter = new ParseVehicleArrayAdapter(getActivity(), new ArrayList<ParseVehicle>());		
		setListAdapter(myVehicleAdapter);
		
	    this.setMenuVisibility(true);
	    registerForContextMenu(getListView());
	    
		if (ParseUser.getCurrentUser() != null &&
				ParseUser.getCurrentUser().isAuthenticated() ){ // User was authenticated
			
		    refreshList();

		}else{ // Need sign in/up
			Toast.makeText(getActivity(), "You need to sign in in My Profile first!!", Toast.LENGTH_LONG).show();
		}
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//Creates the Edit menu for the specific option
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.menu_vehicle, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	 
	    switch (item.getItemId()) {
	    case R.id.edit_item:
	    	EditVehicleFragment.editVehicle(getFragmentManager(), info.position );
	    	return true;
	    	
	    case R.id.remove_item:
	    	removeVehicle(info.position);
	        return true;
	    }
	    return false;
	}
	
	//Should be called only when ParseUser.getCurrentUser() is authenticated
	public static void refreshList(){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Vehicle");
		query.whereContains(ParseVehicle.OWNERID, ParseUser.getCurrentUser().getObjectId());
		query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
		
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e==null){
					
					myVehicleAdapter.clear();
					
					for (int i=0; i < objects.size(); i++){
						myVehicleAdapter.add( (ParseVehicle) objects.get(i) );
					}
				}else{
					Toast.makeText(myContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private static void removeVehicle(final int position){
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	try {
		        		//remove from Parse
		        		myVehicleAdapter.getItem(position).delete();
		        		
						//remove from the list
			        	myVehicleAdapter.remove( myVehicleAdapter.getItem(position) );
			        	myVehicleAdapter.notifyDataSetChanged();
			        	ParseQuery.clearAllCachedResults();
			        	refreshList();
		        	} catch (ParseException e) {
						Toast.makeText(myContext, "Error removing: " + e.getMessage() , Toast.LENGTH_SHORT).show();
					}
		        	
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
		builder.setMessage("Confirm removing selected vehicle?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		
	}
}
