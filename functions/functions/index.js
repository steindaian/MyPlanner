const functions = require('firebase-functions');
// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

exports.triggerFirestore = functions.firestore
	.document('users/{userId}')
	.onUpdate((change,context) => {
		const userAfterReq = change.after.data();
		const userBeforeReq = change.before.data();
		
		const arrayOfReqUsers = userAfterReq.requests;
		const token = userAfterReq.token;
		
		var message = {
			data: {
				requests : arrayOfReqUsers.join(",")
			},
			token: token
		};
		if (token && arrayOfReqUsers && arrayOfReqUsers.length > 0) {
			if(arrayOfReqUsers.length > userBeforeReq.requests.length || (userBeforeReq.token && userBeforeReq.token !== token)) {
			
				admin.messaging().send(message)
				  .then((response) => {
					// Response is a message ID string.
					console.log('Successfully sent message:', response);
					return 0;
				  })
				  .catch((error) => {
					console.log('Error sending message:', error);
					return 1;
				  });
			}
		}
		return 2;
		
	});
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
