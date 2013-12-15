package com.example.ridekeeper;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class VehicleViewFragment extends Fragment {
	
	private static final String IMAGE_UNSPECIFIED = "image/*";
	
	private Uri mImageCaptureUri;
	private ImageView mImageView;
	private boolean isTakenFromCamera;
	
	private TextView name,
		email,
		make,
		model,
		year,
		license;
	
	public static final String USER_NAME="user_name";
	public static final String MY_USER_NAME_HACKFORNOW="jried";
	public static final String EMAIL="email";
	public static final String NAME="name";
	public static final String MODEL="model";
	public static final String MAKE="make";
	public static final String YEAR="year";
	public static final String PHOTO="photo";
	public static final String LICENSE="license";
	
	
	   @Override
	    public View onCreateView(LayoutInflater inflater, 
          ViewGroup container, Bundle savedInstanceState) {
	        // Inflate the layout for this fragment
	        View view =  inflater.inflate(R.layout.fragment_vehicle_view, container, false);
	        
	        make = (EditText) view.findViewById(R.id.vehicle_item_make);
	        model = (EditText) view.findViewById(R.id.vehicle_item_model);
	        year = (EditText) view.findViewById(R.id.vehicle_item_year);
	        license = (EditText) view.findViewById(R.id.vehicle_liscense);

			mImageView = (ImageView) view.findViewById(R.id.vehicle_item_photo);
			
	        Bundle parameters = this.getArguments();
	        if(parameters != null){
	        	String makeVal = parameters.getString(MAKE),
	        			modelVal = parameters.getString(MODEL),
	        			yearVal = parameters.getString(YEAR),
	        			licenseVal = parameters.getString(LICENSE);
	        	
	        	byte []photoVal = parameters.getByteArray(PHOTO);
	        	
	        	make.setText(makeVal == null ? "Enter value":makeVal);
	        	model.setText(makeVal == null ? "Enter value":modelVal);
	        	year.setText(makeVal == null ? "Enter value":yearVal);
	        	license.setText(makeVal == null ? "Enter value":licenseVal);
	        	
	            Bitmap bitmap = BitmapFactory.decodeByteArray(photoVal, 0, photoVal.length);
	            mImageView.setImageBitmap(bitmap);
	        }
	        return view;
	   }
}
