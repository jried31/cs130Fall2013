package com.example.ridekeeper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class DialogFragmentMgr {
	public static void showDialogFragment(Activity activity, DialogFragment fragment, String dialogName, boolean cancelable, Bundle bundle){
    	FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
    	Fragment prev = activity.getFragmentManager().findFragmentByTag(dialogName);
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
    	fragment.setCancelable(cancelable);
    	if (bundle != null){
    		fragment.setArguments(bundle);
    	}
    	fragment.show(ft, dialogName);
    	
	}
}
