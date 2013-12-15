package com.example.ridekeeper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MyVehicleFormFragment extends Fragment {
	public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
	public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;
	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 100;
	public static final int REQUEST_CODE_CROP_PHOTO = 101;
	public static final int REQUEST_CODE_SELECT_FROM_GALLERY = 102;
	
	private static final String IMAGE_UNSPECIFIED = "image/*";
	private static final String URI_INSTANCE_STATE_KEY = "saved_uri";

	private Uri mImageCaptureUri;
	private ImageView mImageView;
	private boolean isTakenFromCamera;
	
	private EditText make,
		model,
		year,
		license;
	
	private Button save,
		change;
	public static final String USER_NAME="user_name";
	public static final String MY_USER_NAME_HACKFORNOW="jried";
	public static final String EMAIL="email";
	public static final String NAME="name";
	public static final String MAKE="make";
	public static final String MODEL="model";
	public static final String YEAR="year";
	public static final String LICENSE="license";
	
	public static final String PHOTO="photo";
	
	private SharedPreferences sharedPreferences;
	
	   @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        // Inflate the layout for this fragment
	        View view =  inflater.inflate(R.layout.fragment_vehicle_form, container, false);
	        
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
			
	        save = (Button) view.findViewById(R.id.button_save);
		    save.setOnClickListener(new View.OnClickListener() {
		    	public void onClick(View v) {
		    		//Save the Content to the Shared Preferences & Upload to Firebase
		    		Toast.makeText(v.getContext(), "I've been clicked" ,4000).show();
		    		/*
		    		Editor editor = sharedPreferences.edit();
		    		String nameVal = name.getText().toString(),
		    				emailVal = email.getText().toString(),
		    				phoneVal = phone.getText().toString();
		            		
		    		// Save profile image into internal storage.
		    		mImageView.buildDrawingCache();
		    		Bitmap bmap = mImageView.getDrawingCache();
		    		try {
		    			//NOTE MODE_PRIVATE saves teh image file as a private file with the name designated under photo_filename
		    			FileOutputStream fos = getActivity().openFileOutput(
		    					getString(R.string.photo_filename), Activity.MODE_PRIVATE);
		    			bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
		    			fos.flush();
		    			fos.close();
		    		} catch (IOException ioe) {
		    			ioe.printStackTrace();
		    		}
			
		    		//Grab the Preference Object
		    		editor.putString(NAME, nameVal);
		    		editor.putString(EMAIL, emailVal);
		    		editor.putString(PHONE, phoneVal);
		    		editor.commit();
		            	
		    		//Update Firebase 
		    		// Create a reference to a Firebase location
		    		String url = "https://ridekeepr.firebaseio.com/users/"+MY_USER_NAME_HACKFORNOW;
		    		Firebase fireBase = new Firebase(url);
		    		fireBase.child(NAME).setValue(nameVal);
		    		fireBase.child(EMAIL).setValue(emailVal);
		    		fireBase.child(PHONE).setValue(phoneVal);
		    		//TODO: DECIDE WHETHER TO SAVE IMAGE IN FIREBASE
		    		 * 
		    		 * 
		    		 */
		    	}
		    });

	       	//Setup for Changing Profile Picture
	        change = (Button) view.findViewById(R.id.button_change);
	        change.setOnClickListener(new View.OnClickListener() {
	        	public void onClick(View v) {
	        		//Save the Content to the Shared Preferences & Upload to Firebase
	        		Toast.makeText(v.getContext(), "Change I've been clicked" ,4000).show();
	        		
	        		final Activity parent = getActivity();
	        		AlertDialog.Builder builder = new AlertDialog.Builder(parent);
	        		DialogInterface.OnClickListener dlistener;
	        		builder.setTitle(R.string.photo_picker_title);
	        		dlistener = new DialogInterface.OnClickListener() {
	        			public void onClick(DialogInterface dialog, int item) {
	        				onPhotoPickerItemSelected(item);					
	        			}
	        		};
	    				
	        		builder.setItems(R.array.photo_picker_items, dlistener);
	        		builder.create().show();//.show(getFragmentManager(),getString(R.string.photo_picker_tag));
		    				
	        	}
	        });
	        return view;
	   }
	   
		public void onPhotoPickerItemSelected(int item) {
			Intent intent;
			isTakenFromCamera = false;
			
			switch(item){
			case MyVehicleFormFragment.ID_PHOTO_PICKER_FROM_CAMERA:
				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mImageCaptureUri = Uri.fromFile(new File(Environment
						.getExternalStorageDirectory(), "tmp_"
						+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
						mImageCaptureUri);
				intent.putExtra("return-data", true);
				try {
					startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
				isTakenFromCamera = true;
				break;
				
			case MyVehicleFormFragment.ID_PHOTO_PICKER_FROM_GALLERY:
				intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				mImageCaptureUri = Uri.fromFile(new File(Environment
						.getExternalStorageDirectory(), "tmp_"
						+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
						mImageCaptureUri);
				intent.putExtra("return-data", true);
				try{
					startActivityForResult(intent, REQUEST_CODE_SELECT_FROM_GALLERY);
				}catch(ActivityNotFoundException e){
					e.printStackTrace();
				}
				isTakenFromCamera = false;
				break;
				
			default:
				return;
			}
		}
		

		// Handle data after activity returns.
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			
			if (resultCode != Activity.RESULT_OK)
				return;

			switch (requestCode) {
			case REQUEST_CODE_SELECT_FROM_GALLERY:
				mImageCaptureUri = data.getData();
				
			case REQUEST_CODE_TAKE_FROM_CAMERA:
				// Send image taken from camera for cropping
				cropImage();
				break;
				
			case REQUEST_CODE_CROP_PHOTO:
				// Update image view after image crop

				Bundle extras = data.getExtras();

				// Set the picture image in UI
				if (extras != null) {
					mImageView.setImageBitmap((Bitmap) extras.getParcelable("data"));
				}

				// Delete temporary image taken by camera after crop.
				if (isTakenFromCamera) {
					File f = new File(mImageCaptureUri.getPath());
					if (f.exists())
						f.delete();
				}

				break;
			}
		}

		private void loadSnap() {
			// Load profile photo from internal storage
			try {
				FileInputStream fis = getActivity().openFileInput("avatar.png");
				Bitmap bmap = BitmapFactory.decodeStream(fis);
				mImageView.setImageBitmap(bmap);
				fis.close();
			} catch (IOException e) {
				// Default profile photo if no photo saved before.
				mImageView.setImageResource(R.drawable.avatar);
			}
		}
		
		private void saveSnap() {

		// Commit all the changes into preference file
			// Save profile image into internal storage.
			mImageView.buildDrawingCache();
			Bitmap bmap = mImageView.getDrawingCache();
			try {
				//NOTE MODE_PRIVATE saves teh image file as a private file with the name designated under photo_filename
				FileOutputStream fos = getActivity().openFileOutput(
						"avatar.png", Activity.MODE_PRIVATE);
				bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		// Crop and resize the image for profile
		private void cropImage() {
			// Use existing crop activity.
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

			// Specify image size
			intent.putExtra("outputX", 100);
			intent.putExtra("outputY", 100);

			// Specify aspect ratio, 1:1
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);
			// REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
			// identify the activity in onActivityResult() when it returns
			startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
		}

}
