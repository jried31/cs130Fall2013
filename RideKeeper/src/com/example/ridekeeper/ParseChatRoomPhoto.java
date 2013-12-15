package com.example.ridekeeper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;

@ParseClassName(DBGlobals.PARSE_CHATROOMPHOTO_TBL)
public class ParseChatRoomPhoto extends ParseObject {
	public static final String	VEHICLEID = "vehicleId",
			 					PHOTO = "photo";
	
	private static final String PHOTOFILE_PREFIX = "chatroom_photo_",
								PHOTOFILE_SUBFIX = ".png";
	
	private Context myContext;
	private byte[] photoData;
	
	public ParseChatRoomPhoto() {
	}

	public String getVehicleId(){
		return getString(VEHICLEID);
	}
	public void setVehicleId(String id){
		put(VEHICLEID, id);
	}

	public ParseFile getPhoto(){
		return getParseFile(PHOTO);
	}
	
	void setPhoto(ParseFile photo){
		put(PHOTO, photo);
	}
	
	//Preparing the photo data to be saved to Parse
	public void prepareSavingPhoto(Context contexct, ParseImageView mImageView){
		mImageView.buildDrawingCache();
		Bitmap bmap = mImageView.getDrawingCache();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
		photoData = bos.toByteArray();
		
		setPhoto( new ParseFile("photo.png", photoData) );
	}
	
	//Preparing the photo data to be saved to Parse
	public void prepareSavingPhoto(Context contexct, Bitmap bmap){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
		photoData = bos.toByteArray();
		
		setPhoto( new ParseFile("photo.png", photoData) );
	}
	
	//Save the photo locally
	public void savePhotoLocally(Context context){
		if (photoData != null){
			try {
				FileOutputStream fos = context.openFileOutput(
						PHOTOFILE_PREFIX + getObjectId() + PHOTOFILE_SUBFIX, Activity.MODE_PRIVATE);
				fos.write(photoData);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e2){
				e2.printStackTrace();
			}
		}
	}
	
	
	public void loadPhotoIntoParseImageView(Context context, ParseImageView mImageView ) {
		myContext = context;
		// Try to load profile photo from internal storage first
		try {
			FileInputStream fis = context.openFileInput( PHOTOFILE_PREFIX + getObjectId() + PHOTOFILE_SUBFIX );
			Bitmap bmap = BitmapFactory.decodeStream(fis);
			mImageView.setImageBitmap(bmap);
			fis.close();
			return;
		} catch (IOException e) {
			// Default profile photo if no photo saved before.
			mImageView.setImageResource(R.drawable.avatar);
		}
		
		// Load from Parse if fail to load from storage
	    ParseFile photo = getPhoto();
	    
	    if (photo!=null){
	    	mImageView.setParseFile(photo);
	    	mImageView.loadInBackground( new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					//save to local disk
					photoData = data;
					savePhotoLocally(myContext);
				}
			});
	    }
	    
	}
}
