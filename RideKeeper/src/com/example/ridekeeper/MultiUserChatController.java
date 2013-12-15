package com.example.ridekeeper;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.module.chat.QBChat;

//Used as a callback after making connection to chat server
interface AfterConnectCallback{
	void done(String errorMsg);
}

public class MultiUserChatController {
	public final String ROOM_SUBFIX = "@muc.chat.quickblox.com";
	
	private String roomname; // should be in the form of <app_id>_name
	private String userJID;  // should be in the form of '17744-1028' (<qb_user_id>-<qb_app_id>)
	private String password;
	private String nickname;  // the nickname used in the chat room
	
	public MultiUserChat muc = null;
	private static Connection connection = null;
	private Context myContext;
	
	public MultiUserChatController(Context context, String roomname, String userJID, String password, String nickname) {
		myContext = context;
		this.roomname = roomname;
		this.userJID = userJID;  
		this.password = password;
		this.nickname = nickname;
	}
	
	private class ChatServerConnector extends AsyncTask<AfterConnectCallback, Void, Object>{
		AfterConnectCallback callback;
		
		@Override
		protected Object doInBackground(AfterConnectCallback... params) {
			callback = params[0];
			ConnectionConfiguration config = new ConnectionConfiguration(QBChat.getChatServerDomain()); //***Must run in a thread***
			connection = new XMPPConnection(config);
			Connection.DEBUG_ENABLED = true;
			
			try {
				//setup connection
	        	connection.connect();
	        	connection.login(userJID, password);
	        	//connection.loginAnonymously();
	        	
				return null;
			} catch (XMPPException e) {
				Log.d("CHATROOM", "Failed to connect/join chat server. Error: " + e.getMessage());
				return e.getMessage();
			}
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (result == null){ //successfully connected
				callback.done(null);
			}else{
				callback.done(result.toString());
			}
		}
	}
	
	public void connect(final AfterConnectCallback afterConnectCallback){
		if ( (connection == null) || (!connection.isConnected())){
			//start the thread to connect to chat server
			new ChatServerConnector().execute(afterConnectCallback);
		}else{
			afterConnectCallback.done(null);
		}
	}
	
	public void disconnect(){
		if (connection!=null){
			connection.disconnect();
		}
	}
	
	public void join() throws XMPPException{
		muc = new MultiUserChat(connection, roomname + ROOM_SUBFIX);
		muc.join(nickname);
	}
	
	public void leaveRoom(){
		if (muc != null){
			muc.leave();
		}
	}
	
	public void sendMessage(String msgString){
		if ( (muc!=null) && (muc.isJoined())){
			try {
				muc.sendMessage(msgString);
				Toast.makeText(myContext, "Message sent", Toast.LENGTH_SHORT).show();
			} catch (XMPPException e) {
				Toast.makeText(myContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	/*
	public void sendMessage(String msgString, Message.Type type){
		if ( (muc!=null) && (muc.isJoined())){
			try {
				Message msg = muc.createMessage();
				msg.setType(type);
				msg.setBody(msgString);
				muc.sendMessage(msg);
				Toast.makeText(myContext, "Message sent", Toast.LENGTH_SHORT).show();
			} catch (XMPPException e) {
				Toast.makeText(myContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	*/

	public void addMsgListener(PacketListener pl){
		muc.addMessageListener(pl);
	}
	
	public void removeMsgListener(PacketListener pl){
		muc.removeMessageListener(pl);
	}
}
