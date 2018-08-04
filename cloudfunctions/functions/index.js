const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.sendNotification = functions.database.ref('/notifications/{notification_id}').onWrite( (change,context)=> {

  const notificationData = change.after.val();

  const fromUid = notificationData.fromUid;
  const from = notificationData.from;
  const to = notificationData.to;
  const content = notificationData.content;

  const payload = {
    notification: {
      title: from,
      body: content,
      icon: 'default',
      sound: 'default'
    },
    data: {
      user_profile_uid: fromUid,
      user_profile_name: from
    }
  };

  // Set the message as high priority and have it expire after 24 hours.
  const options = {
    collapseKey: 'demo',
    contentAvailable: true,
    priority: 'high',
    timeToLive: 60 * 60 * 24,
  };


  const deviceToken = admin.database().ref(`/users/${to}/deviceToken`).once('value');

  return deviceToken.then(result => {
    const token_id = result.val();
    return admin.messaging().sendToDevice(token_id, payload).then(response => {
      console.log("Notification send");
      return response;
    })
  });

});
