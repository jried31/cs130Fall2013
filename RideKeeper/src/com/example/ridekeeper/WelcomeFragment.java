package com.example.ridekeeper;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ridekeeper.account.MyQBUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

public class WelcomeFragment extends DialogFragment {
	private Button btSignup, btSignin, btResetpwd, btExit;
	private EditText etUsername, etPwd, etEmail = null;
	private ParseUser parseSigningUpUser;
	public static final String LAST_SIGNIN_USERNAME="last_username";

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" 
		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String ALPHANUMERIC_PATTERN = "^.*[^a-zA-Z0-9 ].*$";
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_welcome, container, false);
		
		btSignup = (Button) view.findViewById(R.id.button_signup);
		btSignin = (Button) view.findViewById(R.id.button_signin);
		btResetpwd = (Button) view.findViewById(R.id.button_resetpwd);
		btExit = (Button) view.findViewById(R.id.button_exit);

		etUsername = (EditText) view.findViewById(R.id.editText_username);
		etPwd = (EditText) view.findViewById(R.id.editText_pwd);

		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getActivity());
		etUsername.setText( prefs.getString(LAST_SIGNIN_USERNAME, "") );

		btSignup.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = etUsername.getText().toString().trim(),
						password = etPwd.getText().toString().trim();
				
				//remove white spaces (if any)
				username=username.replaceAll("\\s+", "");
				password=password.replaceAll("\\s+", "");
				
				if (username.isEmpty() || password.isEmpty()){
					Toast.makeText(getActivity(), "Username or password cannot be empty", Toast.LENGTH_LONG).show();
					return;
				}
				
				//Check for characters other than Alphanumeric
				boolean special_chars = username.matches(ALPHANUMERIC_PATTERN);
				if(special_chars){
					Toast.makeText(getActivity(), "Special characters or spaces are not allowed", Toast.LENGTH_LONG).show();
					return;
				}
				
				if ( username.length() > 20 || password.length() > 20){
					Toast.makeText(getActivity(), "Username and password should be 20 characters max", Toast.LENGTH_LONG).show();
					return;
				}
				
				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences( v.getContext() );
				prefs.edit().putString(LAST_SIGNIN_USERNAME, etUsername.getText().toString()).commit();
				
				//Next obtain user's email
				showSignUpEmailInputDialog();
			}

		});

		btSignin.setOnClickListener(  new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				btSignin.setEnabled(false);
				String username = etUsername.getText().toString().trim(),
						password = etPwd.getText().toString().trim();
				
				//remove white spaces (if any)
				username=username.replaceAll("\\s+", "");
				password=password.replaceAll("\\s+", "");
				
				if (username.isEmpty() || password.isEmpty()){
					Toast.makeText(getActivity(), "Username or password cannot be empty", Toast.LENGTH_LONG).show();
					btSignin.setEnabled(true);
					return;
				}
				
				//Check for characters other than Alphanumeric
				boolean special_chars = username.matches(ALPHANUMERIC_PATTERN);
				if(special_chars){
					Toast.makeText(getActivity(), "Special characters or spaces are not allowed", Toast.LENGTH_LONG).show();
					btSignin.setEnabled(true);
					return;
				}
				
				if ( username.length() > 20 || password.length() > 20){
					Toast.makeText(getActivity(), "Username and password should be 20 characters max", Toast.LENGTH_LONG).show();
					btSignin.setEnabled(true);
					return;
				}
				
				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(v.getContext());
				prefs.edit().putString(LAST_SIGNIN_USERNAME, username).commit();

				ParseUser.logInInBackground(etUsername.getText().toString(),
						etPwd.getText().toString(),
						new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException e) {
						if (e == null){
							//HelperFuncs.parseUser = user;
							ParseFunctions.updateOwnerIdInInstallation();
							
							//sign into QB
							MyQBUser.signin(etUsername.getText().toString(), MyQBUser.DUMMY_PASSWORD);
							
							//update phone's location to parse
							ParseFunctions.updateLocToParse(getActivity());
							Toast.makeText(getActivity(), "You are now signed in!", Toast.LENGTH_LONG).show();
							MyProfileFragment.reloadFragment(getActivity());
							dismiss();
						}else{
							Toast.makeText(getActivity(), "Error signing in. " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
						btSignin.setEnabled(true);
					}
				});
			}
		});

		btResetpwd.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showResetEmailInput();
			}
		});

		btExit.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				App.bReceiver.cancelAlarm(getActivity());
				System.exit(0);
			}
		});
		
		return view;
	}


	private void showSignUpEmailInputDialog(){
		etEmail = new EditText(getActivity());

		AlertDialog.Builder alertDialogEmailInput = new AlertDialog.Builder(getActivity());
		alertDialogEmailInput.setTitle(R.string.button_signup);
		alertDialogEmailInput.setMessage(R.string.user_profile_email);
		alertDialogEmailInput.setView(etEmail);
		alertDialogEmailInput.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			//Start signing up process
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String email = etEmail.getText().toString().trim(),
					username = etUsername.getText().toString().trim(),
					password = etPwd.getText().toString().trim();
					email=email.replaceAll("\\s+", "");
				username=username.replaceAll("\\s+", "");
				password=password.replaceAll("\\s+", "");
				
				if (username.isEmpty() || password.isEmpty()){
					Toast.makeText(getActivity(), "Username or password cannot be empty", Toast.LENGTH_LONG).show();
					btSignin.setEnabled(true);
					return;
				}
				
				//Check for characters other than Alphanumeric
				boolean special_chars = username.matches(ALPHANUMERIC_PATTERN);
				if(special_chars){
					Toast.makeText(getActivity(), "Special characters or spaces are not allowed", Toast.LENGTH_LONG).show();
					btSignin.setEnabled(true);
					return;
				}
				
				if ( username.length() > 20 || password.length() > 20){
					Toast.makeText(getActivity(), "Username and password should be 20 characters max", Toast.LENGTH_LONG).show();
					btSignin.setEnabled(true);
					return;
				}
				
				//Check for characters other than Alphanumeric
				if (email.isEmpty()){
					Toast.makeText(getActivity(), "Email must not be empty", Toast.LENGTH_LONG).show();
					return;
				}
				
				if(email.matches(EMAIL_PATTERN) == false){
					Toast.makeText(getActivity(), "Invalid email", Toast.LENGTH_LONG).show();
					return;
				}
				btSignup.setEnabled(false);

				//display progress dialogue for user account signup
				ProgressDialog progressDialog = new ProgressDialog(getActivity());
				progressDialog.setCancelable(false);
				progressDialog.setMessage(getString(R.string.signup_status));
				
				parseSigningUpUser = new ParseUser();
				parseSigningUpUser.setUsername( username);
				parseSigningUpUser.setPassword( password );
				parseSigningUpUser.setEmail( email );
				parseSigningUpUser.signUpInBackground( new SignUpCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							//Update ownerId in Installation table
							ParseFunctions.updateOwnerIdInInstallation();

							//Signup QB user
							MyQBUser.signUpSignin(etUsername.getText().toString(), MyQBUser.DUMMY_PASSWORD);
							
							//Update phone location to Parse
							ParseFunctions.updateLocToParse(getActivity());
							
							Toast.makeText(getActivity(), "Your account has been created.", Toast.LENGTH_LONG).show();
							MyProfileFragment.reloadFragment(getActivity());
							dismiss();
						}else{
							Toast.makeText(getActivity(), "Error signing up. " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
						btSignup.setEnabled(true);
					}
				});
			}
		});

		alertDialogEmailInput.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		alertDialogEmailInput.show();
	}
	
	private void showResetEmailInput(){
		etEmail = new EditText(getActivity());

		AlertDialog.Builder alertDialogEmailInput = new AlertDialog.Builder(getActivity());
		alertDialogEmailInput.setTitle("Reset Password");
		alertDialogEmailInput.setMessage("Your E-mail:");
		alertDialogEmailInput.setView(etEmail);
		alertDialogEmailInput.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			//Start reseting password process
			@Override
			public void onClick(DialogInterface dialog, int which) {
				btResetpwd.setEnabled(false);
				ParseUser.requestPasswordResetInBackground(etEmail.getText().toString(), new RequestPasswordResetCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							Toast.makeText(getActivity(), "Password reset instruction has been sent to your email." , Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
						btResetpwd.setEnabled(true);
					}
				});
			}
		});

		alertDialogEmailInput.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		alertDialogEmailInput.show();
	}
}
