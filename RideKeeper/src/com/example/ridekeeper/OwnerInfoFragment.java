package com.example.ridekeeper;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;

public class OwnerInfoFragment extends DialogFragment {
	private String profileId = null;	//VBS UID to be tracked, null for all VBS
	private static final String TITLE = "Owner Information";
	private TextView name, email, phone;
	public static final String EMAIL="email";
	public static final String USERNAME="username";
	public static final String PHONE="phone";
	
	//private SharedPreferences sharedPreferences;
	
	public static void reloadFragment(Activity activity){
		activity.getFragmentManager().beginTransaction().replace(R.id.content_frame, new OwnerInfoFragment()).commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		
    	//Load  UID argument for tracking
    	if (getArguments()!=null && getArguments().containsKey("UID")){
        	profileId = getArguments().getString("UID");
        	ParseUser puser;
			try {
				puser = ParseUser.getQuery().get(profileId);

	    		name.setText(puser.getString(USERNAME));
	    		email.setText(puser.getEmail());
	    		phone.setText(puser.getString(PHONE));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
		View view;
		
		if (ParseUser.getCurrentUser() != null &&
				ParseUser.getCurrentUser().isAuthenticated() ){ // User was authenticated
			
			view =  inflater.inflate(R.layout.fragment_owner_info_public, container, false);

			getDialog().setTitle(TITLE);
			
			loadProfile(view);
		}else{ // Need sign in/up
			DialogFragmentMgr.showDialogFragment(getActivity(), new WelcomeFragment(), "Map Dialog", false, null);
			view = inflater.inflate(R.layout.fragment_blank, container, false);
		}
		
		return view;
	}	
	
	private void loadProfile(View view){
		name = (TextView) view.findViewById(R.id.user_profile_name);
		email = (TextView) view.findViewById(R.id.user_profile_email);
		phone = (TextView) view.findViewById(R.id.user_profile_phone);
		
		ParseUser puser =  ParseUser.getCurrentUser();

		name.setText(puser.getString(USERNAME));
		email.setText(puser.getEmail());
		phone.setText(puser.getString(PHONE));
	}
}
