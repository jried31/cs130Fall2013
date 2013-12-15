var restify 		= require('restify');
var request 		= require('request');
var kaiseki_inc 	= require('kaiseki');
var xmpp			= require('node-xmpp');

var jabber_creds = {
	username: 'admin',
	password: 'ilikeorange',
	jid: 'admin-5111@chat.quickblox.com',
	room_jid: '5111_%s@muc.chat.quickblox.com',
	host: 'chat.quickblox.com',
	post: 5222
}

// Avoid some of the magic number BS that surrounds AlertLevel
var AlertLevel = {
	OK: 	0, // do nothing
	TLT: 	1, // notify owner.
	MVT: 	2, // new "stolen" value. notify everyone
	LOC: 	3, // vehicle is in motion.,
	NRD:    "NRD", // not recovered
	RVD: 	"RVD", // recovered
}

// instantiate
var APP_ID = 'OZzFan5hpI4LoIqfd8nAJZDFZ3ZLJ70ZvkYCNJ6f';
var REST_API_KEY = 'bPlqPguhK51mbRXaYcfnf73uTri07sk6uB64ZdPb';
var kaiseki = new kaiseki_inc(APP_ID, REST_API_KEY);

/**
 * A 'cronjob' to run the notify task. Yeah,
 * it leaks memory.
 */
 var minutes = 1, interval = minutes * 1000 * 60;
 setInterval(notifyNearbyUsers, interval);


/**
* Return a timestamp with the format 'm/d/yy h:MM:ss TT'
* @type {Date}
*/

function timestamp()
{
	var d 		= new Date();
	var month 	= d.getMonth() + 1;
	var date 	= d.getDate();
	var year 	= d.getFullYear();
	var hour 	= d.getHours() - 2;
	var min 	= d.getMinutes();
	var sec 	= d.getSeconds();

	if(month < 10)
		month = '0' + month;
	if(date < 10)
		date = '0' + date;
	if(hour < 10)
		hour = '0' + hour;
	if(min < 10)
		min = '0' + min;
	if(sec < 10)
		sec = '0' + sec;

	return month + '-' + date + '-' + year + ' ' + hour + ':' + min + ':' + sec;
}

/**
 * updateVehicleStatus
 *
 * Updates the status of a vehicle. Generally called by the GeogramONE, using
 * information read from the sensor.
 *
 * @param string 	id 				Vehicle ID
 * @param string 	status			New vehicle status.
 * @param GeoPoint 	location		Vehicle location
 */
function updateVehicleStatus(req,resp,next){
	console.log('Got Data %s \n',req.body.id);
	var tmpObj = req.body;
	var position = {location: {
			__type: 'GeoPoint',
			latitude: parseFloat(tmpObj.lat),
			longitude: parseFloat(tmpObj.lng)
		}
	};
  
	kaiseki.updateObject('Vehicle', tmpObj.id,
	                   {'alertLevel': AlertLevel.getKeyByValue(parseInt(tmpObj.alertLevel)), 
	                    'pos': position.location},
	          function(err, res, body, success) {
	          	if (success) {
	          		console.log('Marked ' + tmpObj.id + ' as ' + AlertLevel.getKeyByValue(parseInt(tmpObj.alertLevel)));
	          		sendTiltNotification(tmpObj.id);
	          		sendStolenNotification(tmpObj.id);
	          	} else {
					console.log(body.error);
	          	}
	          }
	);
	resp.send('ok');
}
var server = restify.createServer();
server.use(restify.bodyParser({ mapParams: false }));

server.post('/update', updateVehicleStatus);

server.listen(8080, function() {
	console.log('%s listening at %s', server.name, server.url);
});


/**
 * sendTiltNotification
 *
 * If necessary, notifies the owner of a vehicle that their vehicle
 * has been tilted.
 *
 * @param 	string		id 			Vehicle ID
 */
 function sendTiltNotification(id) {
	// first, fetch vehicle info
	kaiseki.getObject('Vehicle', id, { }, function(err, res, body, success) {
		// then, send the owner notification if the vehicle is tilted
		if (body.alertLevel == "TLT") {
			console.log(id + ' tilted. Notifying owner.');
			var notification_data = {
				where: { objectId: body.ownerId },
				data: {
					action: 'CUSTOMIZED',
					alertLevel: 'TLT',
					vehicleName: body.make + ' ' + body.model
				}
			};
			kaiseki.sendPushNotification(notification_data, function(err, res, body, success) {
				if (success) {
					kaiseki.updateObject('Vehicle', id, { status: 'OK' }, function(err, res, body, success) {
						if (success)
							console.log('Owner notified.');
						else
							console.log(body.error);
					});
				}
				else {
					console.log(body.error);
				}
			});
		}
	});
 }

/**
 * sendStolenNotification
 *
 * If necessary, notifies the owner of a vehicle that their vehicle has
 * been stolen. This will also create the chatroom for the vehicle.
 *
 * @param 	string		id 		Vehicle ID
 */
function sendStolenNotification(id) {
	// first, fetch vehicle info
	kaiseki.getObject('Vehicle', id, { }, function(err, res, body, success) {
		// then, send the owner notification if the vehicle is tilted
		if (body.alertLevel == "MVT") {
			console.log(id + ' stolen! Notifying owner.');
			var notification_data = {
				where: { objectId: body.ownerId },
				data: {
					action: 'CUSTOMIZED',
					alertLevel: 'MVT',
					vehicleName: body.make + ' ' + body.model
				}
			};
			kaiseki.sendPushNotification(notification_data, function(err, res, body, success) {
				if (success) {
					console.log('Owner notified.');
					createChatroom(id);
				}
				else {
					console.log(body.error);
				}
			});
		}
	});
}

/**
 * createChatroom
 *
 * Creates a chatroom for the stolen vehicle.
 *
 * @param 	string		id 		Vehicle ID
 */
function createChatroom(id) {
	kaiseki.createObject('Chatroom', { vehicleId: id }, function(err, res, body, success) {
		if (success) {
			create_chatroom(body.objectId);
			console.log('Created chatroom for ' + id);
		}
		else
			console.log(body.error);
	});
}

/**
 * notifyNearbyUsers()
 *
 * Cycle through every stolen vehicle and notify users within
 * .5 miles that the vehicle is stolen.
 */
function notifyNearbyUsers() {
	console.log('Notifying users near stolen vehicles.');
	kaiseki.getObjects('Vehicle', {  where: { alertLevel: "MVT" } }, function(err, res, body, success) {
		if (success) {
			for (var i = 0; i < body.length; ++i) {
				var veh = body[i];
				console.log('Notifying users near ' + veh['objectId']);

				var geopoint_where = {
					GeoPoint: {
						'$nearSphere': {
							__type: 'GeoPoint',
							'latitude': veh['pos']['latitude'],
							'longitude': veh['pos']['longitude']
						},
						'$maxDistanceInMiles': 0.3
					}
				};

				kaiseki.getUsers(geopoint_where, function(err, res, body, success) {
					if (success) {
						var nearby_owners = new Array();
						for (var j = 0; j < body.length; ++j) {
							nearby_owners.push(body[j]['objectId']);
						}
						kaiseki.getObjects('Chatroom', { where: { vehicleId: veh['objectId'] } }, function(err, res, body, success) {
							if (success) {
								for (var k = 0; k < body.length; ++k) {
									var room = body[k];

									if (room['members'] === undefined)
										room['members'] = new Array();

									var new_users = arr_diff(nearby_owners, room['members']);
									var new_members = array_unique(room['members'].concat(nearby_owners));
									// Update chatroom
									kaiseki.updateObject('Chatroom', room['objectId'], { members: new_members }, function(err, res, body, success) {
										if (success) {
											// Notify users
											for (var j = 0; j < new_users.length; ++j) {
												var notification_data = {
													where: { objectId: new_users[j] },
													data: {
														action: 'CUSTOMIZED',
														alertLevel: 'nearby',
														vehicleName: body.make + ' ' + body.model
													}
												};
												kaiseki.sendPushNotification(notification_data, function(err, res, body, success) {
													if (success) {
														// don't want to flood the console...
													}
													else {
														console.log(body.error);
													}
												});
											}
										}
										else {
											console.log(body.error);
										}
									});
								}
							} else {
								console.log(body.error);
							}
						})
					} else {
						console.log(body.error);
					}
				});
			}
		}
		else {
			console.log(body.error);
		}
	});
}

/**
 * create_chatroom
 *
 * Creates an XMPP chatroom with the given chatroom name.
 *
 * @param 	string 	chatroom_id 	The unique chatroom identifier. Should match the Parse objectId.
 */
function create_chatroom(chatroom_id) {
	var cl = new xmpp.Client({
		jid: jabber_creds.jid,
		password: jabber_creds.password,
		host: jabber_creds.host,
		port: jabber_creds.post
	});

	cl.on('online', function() {
		cl.send(new xmpp.Element('iq', { to: jabber_creds.room_jid.replace("%s", chatroom_id), id: 'create', type: 'set' }));
    	c('query', { xmlns: 'http://jabber.org/protocol/muc#owner' });
    	c('x', { xmlns: 'jabber:x:data', type: 'submit' });

    	console.log('Created new XMPP chatroom ' + chatroom_id);
	});
}

function array_unique(array) {
    var a = array.concat();
    for(var i=0; i<a.length; ++i) {
        for(var j=i+1; j<a.length; ++j) {
            if(a[i] === a[j])
                a.splice(j--, 1);
        }
    }

    return a;
};

/**
 * Removes all items in a1 that are also in a2
 */
function arr_diff(a1, a2)
{
  var a=[], diff=[];
  for(var i=0;i<a1.length;i++)
    a[a1[i]]=true;
  for(var i=0;i<a2.length;i++)
    if(a[a2[i]]) delete a[a2[i]];
    else a[a2[i]]=true;
  for(var k in a)
    diff.push(k);
  return diff;
}

/**
 * Finds the key in an object by its value.
 */
Object.prototype.getKeyByValue = function( value ) {
    for( var prop in this ) {
        if( this.hasOwnProperty( prop ) ) {
             if( this[ prop ] === value )
                 return prop;
        }
    }
}
