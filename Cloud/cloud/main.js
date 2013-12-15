// The painful, monolithic file of node.js code. This isn't separated as,
// like many node.js applications, only main.js is loaded on startup. Apparently,
// including jobs as modules can cause fits with scheduled jobs, so we don't do that
// either. Oi vey.

// Send Tilt Notification
// When a vehicle is tilted, we just notify the owner--nothing big.
// It's up to the owner to login and mark the vehicle as stolen.
Parse.Cloud.job( "sendTiltNotification", function( request, status ) {
	Parse.Cloud.useMasterKey();

	var Vehicle = Parse.Object.extend( "Vehicle" );
	var vehicleQuery = new Parse.Query( Vehicle );

	vehicleQuery.equalTo( "tilt", true );

	vehicleQuery.find( {
		success: function( results ) {
			console.log( results.length + " vehicles to notify of tilt." );
			for ( var i = 0; i < results.length; i++ ) {
				var object = results[i];
				var query = new Parse.Query( Parse.Installation );
        		query.equalTo( "ownerId", object.get( 'ownerId' ) );

        		console.log( "Preparing push notification " + i );

        		Parse.Push.send( {
        			where: query,
        			data: {
        				alert: "Your " + object.get( 'make' ) + " " + object.get( 'model' ) + " has been tilted."
        			}
        		}, {
        			success: function() {
        			},
        			error: function( error ) {
        				status.error( error );
        			}
        		} );

        		    status.message( "Successfully notified vehicle " + i + " of tilt. " );

    				object.set( "tilt", false ); // we undo the status so we don't send duplicates
    				object.save();
			}
			status.success( "Succesfully notified tilted vehicles.") ;
		},
		error: function( error ) {
    		status.error( "Error: " + error.code + " " + error.message );
    	}
	} );
} );

// Update Stolen Status
// Update the stolen status of a vehicle. At this time, we consider
// a stolen vehicle to be any lifted vehicle.
Parse.Cloud.job( "updateStolenStatus", function( request, status ) {
	Parse.Cloud.useMasterKey();

	var Vehicle = Parse.Object.extend( "Vehicle" );
    var Chatroom = Parse.Object.extend( "Chatroom" );
	var vehicleQuery = new Parse.Query( Vehicle );

	vehicleQuery.equalTo( "lift", true );

	vehicleQuery.find( {
		success: function( results ) {
			console.log( results.length + " vehicles to notify of lift." );
			for ( var i = 0; i < results.length; i++ ) {
				var object = results[i];
				var query = new Parse.Query( Parse.Installation );
        		query.equalTo( "ownerId", object.get( 'ownerId' ) );

        		console.log( "Preparing push notification " + i );
        		Parse.Push.send( {
        			where: query,
        			data: {
        				alert: "Your " + object.get( 'make' ) + " " + object.get( 'model' ) + " has been stolen!"
        			}
        		}, {
        			success: function() {
        			},
        			error: function( error ) {
        				status.error ( error );
        			}
        		} );

				object.set( "lift", false ); // we undo the status so we don't send duplicates
				object.set( "stolen", true );

				object.save();

        		// Create the chatroom for the stolen vehicle. Note that we don't add any users here--
                // that's something we do later on, in another job.
                var chatroom = new Chatroom();
                chatroom.set( "vehicleId", object.id );
                chatroom.save();

                console.log( "Created chatroom for vehicle" + object.id );

                status.message( "Successfully notified vehicle " + i + " of tilt. ");


        	}
        	status.success( "Successfully notified stolen vehicles." );
		},
		error: function( error ) {
    		status.error( "Error: " + error.code + " " + error.message );
    	}
	} );
} );

// Add Nearby Users to Chatroom
// Every so often, we need to find users who are nearby and add them to the chatroom.

Parse.Cloud.job( "addNearbyUsersToChat", function( request, status ) { 
    Parse.Cloud.useMasterKey();

    var Chatroom = Parse.Object.extend( "Chatroom" );
    var Vehicle = Parse.Object.extend( "Vehicle" );
    var Installation = Parse.Object.extend( "Installation" );

    var chatroomQuery = new Parse.Query( Chatroom );

    chatroomQuery.find( {
        success: function ( results ) {
            for ( var i = 0; i < results.length; i++ ) {
                var room = results[i];

                // First, we should check if the vehicle is still stolen...
                var vehicleQuery = new Parse.Query( Vehicle );
                vehicleQuery.equalTo( "objectId", room.get( "vehicleId" ) );

                vehicleQuery.find( {
                    success: function( results ) {
                        var vehicle = results[0];

                        if ( false == vehicle.get( "stolen") ) {
                            return; // Onto the next vehicle
                        }
                        else {
                            status.message( "Notifying nearby users of theft of " + vehicle.id );

                            // Since it's still stolen, find nearby users
                            console.log( "Notifying nearby users." );
                            var installQuery = new Parse.Query( Installation );
                            installQuery.withinMiles('GeoPoint', vehicle.pos, 0.5 ); // For now, notify users within half a mile

                            installQuery.find( { 
                                success: function ( results ) {
                                    if ( results.length == 0 ) {
                                        status.message( "No nearby users to notify for " + vehicle.id );
                                    }

                                    for ( var i = 0; i < results.length; i++ ) {
                                        var nearby_user = results[i];

                                        // Now that we've found the user, we need to do two things:
                                        // 1. Add them to the user list, if they're not already there
                                        // 2. Notify them.

                                        status.message( "Adding " + nearby_user.id + " to list for " + vehicle.id );

                                        if ( -1 == room.get( "members" ).indexOf( nearby_user.get( "ownerId" ) ) ) {
                                            // We still use addUnique here for safety, even though we did the check
                                            room.addUnique( "members", nearby_user.get( "ownerId" ) );
                                            room.save();

                                            var query = new Parse.Query( Parse.Installation );
                                            query.equalTo( "ownerId", nearby_user.get( "ownerId" ) );

                                            console.log( "Preparing push notification " + i );
                                            Parse.Push.send( {
                                                where: query,
                                                data: {
                                                    alert: "A nearby vehicle has been stolen!"
                                                }
                                            }, {
                                                success: function() {
                                                },
                                                error: function( error ) {
                                                    status.error ( error );
                                                }
                                            } );
                                        }

                                        status.message( "Notified nearby user " + i + " of theft of " + vehicle.id );
                                    }
                                },
                                error: function( error ) {
                                    status.error( "Error: " + error.code + " " + error.message );
                                }
                            } );
                        }
                    },
                    error: function( error ) {
                        status.error( "Error: " + error.code + " " + error.message );
                    }
                } );
            }
            status.success( "Succesfully added users to chatrooms." );
        },
        error: function( error ) {
            status.error( "Error: " + error.code + " " + error.message );
        }
    } );
} );