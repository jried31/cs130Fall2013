package com.example.ridekeeper;

 class DBGlobals {
	public static final String DB_SCHEMA_NAME = "RIDEKEEPER";
	public static final int 
						VBS_LIST=0,
						MY_PROFILE=1,
						MY_VEHICLE=2,
						SETTINGS=3;
	
	public static final double searchRadius = 10; //radius to scan for VBS (in miles)
	public static final int vehiclePosUpdateInGMapRate = 2000; //update rate for VBS position on the map (in ms);
	public static final int repeatingAlarmRate = 1000 * 60 * 5; //wake up phone and run some tasks every X ms
	
	public static final String PARSE_VEHICLE_TBL = "Vehicle";
	public static final String PARSE_CHATROOM_TBL = "Chatroom";
	public static final String PARSE_CHATROOMPHOTO_TBL = "ChatRoomPhoto";
	public static final String PARSE_INSTL_OWERID = "ownerId";
}