package com.example.ridekeeper;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditVehicleFragment extends DialogFragment {
	// For taking picture:
	public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
	public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;
	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 100;
	public static final int REQUEST_CODE_CROP_PHOTO = 101;
	public static final int REQUEST_CODE_SELECT_FROM_GALLERY = 102;

	private static final String IMAGE_UNSPECIFIED = "image/*";
	private Uri mImageCaptureUri;
	private boolean isTakenFromCamera;
	// End for taking picture
	
	
    private ParseImageView pivPhoto;
    private EditText etMake, etModel, etYear, etLicense;
    private Button btSave, btChangePhoto;
    
	private String mode = "add"; //Default is add mode
    private int pos = 0;
    ParseVehicle vehicle; //current vehicle being add/edit
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	//Checking whether we're adding or editing a vehicle
    	if (getArguments()!=null && getArguments().containsKey("mode")){
    		mode = getArguments().getString("mode");
    		if (mode.equals("edit")){
    			pos = getArguments().getInt("pos");
    		}
    	}

    	setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_NoActionBar  );
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_vehicle, container, false);
		
	    pivPhoto = (ParseImageView) view.findViewById(R.id.edit_vehicle_img);
	    etMake = (EditText) view.findViewById(R.id.editText_make);
	    etModel = (EditText) view.findViewById(R.id.editText_model);
	    etYear = (EditText) view.findViewById(R.id.editText_year);
	    etLicense = (EditText) view.findViewById(R.id.editText_license);
		btSave = (Button) view.findViewById(R.id.button_save_vehicle);
	    btChangePhoto = (Button) view.findViewById(R.id.button_change_vehicle_photo);
		
		if (mode.equals("add")){
			btSave.setText("Add");
			vehicle = new ParseVehicle();

			vehicle.put("ownerId", ParseUser.getCurrentUser().getObjectId());
			
		}else if (mode.equals("edit")){
			vehicle = MyVehicleListFragment.myVehicleAdapter.getItem(pos);
			
			//Load data from Parse
			etMake.setText(vehicle.getMake());
			etModel.setText(vehicle.getModel());
			etYear.setText(vehicle.getYear().toString());
			etLicense.setText(vehicle.getLicense());
			vehicle.loadPhotoIntoParseImageView(getActivity(), pivPhoto);
			
			btSave.setText("Save");
		}

		
		btChangePhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(getActivity() , GetPhoto.class);
				//startActivityForResult(intent, 0);
				//will get the result in onActivityResult
				
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
				builder.create().show();

				
			}
		});
		
		
		btSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btSave.setEnabled(false);
				
				vehicle.setMake(etMake.getText().toString());
				vehicle.setModel(etModel.getText().toString());
				vehicle.setYear( etYear.getText().toString());
				vehicle.setLicense(etLicense.getText().toString());
				vehicle.prepareSavingPhoto(getActivity(), pivPhoto);
				
				if (mode.equals("add")){
					MyVehicleListFragment.myVehicleAdapter.add(vehicle);
				}else if (mode.equals("edit")){
				}
				
				//Add the new vehicle / save modified vehicle
				vehicle.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							//Now the 'objectId' of the vehicle is available.
							vehicle.savePhotoLocally(getActivity());
							
							Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
							
						}else{
							Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
						
						btSave.setEnabled(true);
						
						//MyVehicleListFragment.myVehicleAdapter.notifyDataSetChanged();
						ParseQuery.clearAllCachedResults();
						MyVehicleListFragment.refreshList();
						getFragmentManager().popBackStack(); //Remove the Edit fragment
					}
				});
				
				

			}
		});
		
		return view;
    }
    
    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode==Activity.RESULT_OK){
    		
    		Bundle extras = data.getExtras();
    		if (extras != null) {
    			pivPhoto.setImageBitmap((Bitmap) extras.getParcelable("data"));
			}
    		
    	}
    }*/
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
		case REQUEST_CODE_SELECT_FROM_GALLERY:
			mImageCaptureUri = data.getData();
			cropImage();
			break;

		case REQUEST_CODE_TAKE_FROM_CAMERA:
			// Send image taken from camera for cropping
			cropImage();
			break;

		case REQUEST_CODE_CROP_PHOTO:
			// Update image view after image crop

			Bundle extras = data.getExtras();

			// Set the picture image in UI
			if (extras != null) {
				Bitmap bitmap = (Bitmap) extras.getParcelable("data");
				pivPhoto.setImageBitmap(bitmap);
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
    
    
	public static void addVehicle(FragmentManager fm){
    	FragmentTransaction ft = fm.beginTransaction();
    	Fragment prev = fm.findFragmentByTag("Add Vehicle Dialog");
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
		
    	DialogFragment editVehicleFrag = new EditVehicleFragment();
    	Bundle args = new Bundle();
    	args.putString("mode", "add");
    	editVehicleFrag.setArguments(args);
    	editVehicleFrag.show(ft, "Add Vehicle Dialog");
	}
	
	public static void editVehicle(FragmentManager fm, int position){
    	FragmentTransaction ft = fm.beginTransaction();
    	Fragment prev = fm.findFragmentByTag("Add Vehicle Dialog");
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
		
    	DialogFragment editVehicleFrag = new EditVehicleFragment();
    	Bundle args = new Bundle();
    	args.putString("mode", "edit");
    	args.putInt("pos", position);
    	editVehicleFrag.setArguments(args);
    	editVehicleFrag.show(ft, "Add Vehicle Dialog");
	}
	
	
	//For taking picture from camera / gallery:
	private void onPhotoPickerItemSelected(int item) {
		Intent intent;
		isTakenFromCamera = false;

		switch(item){
		case ID_PHOTO_PICKER_FROM_CAMERA:
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

		case ID_PHOTO_PICKER_FROM_GALLERY:
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
