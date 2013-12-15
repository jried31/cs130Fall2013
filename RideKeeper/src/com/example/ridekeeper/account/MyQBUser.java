package com.example.ridekeeper.account;

import android.content.Context;
import android.content.SharedPreferences;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;

public class MyQBUser {
	private static Context myContext;
	private static QBUser user = null;
	
	//Password for all QBUser accounts
	public static final String DUMMY_PASSWORD = "abcde123";
	//public static boolean sessionCreated = false;
	
	public static void signUpSignin(String username, String password){
		user = new QBUser(username, password);
		QBUsers.signUpSignInTask(user,  new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {}
			@Override
			public void onComplete(Result arg0) {
				if (arg0.isSuccess()){
					saveUserJabberIDtoCache(getUserJabberID());
				}
			}
		});
	}
	
	public static void signin(String username, String password){
		user = new QBUser(username, password);
		QBUsers.signIn(user, new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {}
			@Override
			public void onComplete(Result arg0) {
				if (arg0.isSuccess()){
					saveUserJabberIDtoCache(getUserJabberID());
				}
			}
		});
	}

	public static String getLoginName(){
		if (user != null){
			return user.getLogin();	
		}else{
			return null;
		}
	}
	
	public static String getLoginPassword(){
		if (user != null){
			return user.getPassword();
		}else{
			return null;
		}
	}
	
	//Get the JabberId of the user for login to chat room
	public static String getUserJabberID(){
		if (user != null){
			return QBChat.getChatLoginShort(user);	
		}else{
			return null;
		}
	}
	
	public static String getUserJabberIDfromCache(){
		SharedPreferences pref = myContext.getSharedPreferences("QBUser", 0);
		return pref.getString("userjid", "");
	}
	
	public static void saveUserJabberIDtoCache(String JID){
		SharedPreferences pref = myContext.getSharedPreferences("QBUser", 0);
		pref.edit().putString("userjid", JID).commit();
	}
	
	public static QBUser getCurrentUser(){
		return user;
	}
	
	public static void initContext(Context context){
		myContext = context;
	}
	
}
