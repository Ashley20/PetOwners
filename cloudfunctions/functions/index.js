const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.sendNotification = functions.firestore
  .document('notifications/{docId}')
  .onCreate((snap, context) => {

    const store = admin.firestore();
    let deviceToken

    const newValue = snap.data();

    const fromUid = newValue.fromUid;
    const from = newValue.from;
    const to = newValue.to;
    const content = newValue.content;

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

    store.collection('users').doc(to).get().then(doc => {
      if (doc.exists) {
        deviceToken = doc.data().deviceToken;
        console.log(deviceToken);

        admin.messaging().sendToDevice(deviceToken, payload, options).then(response => {
          console.log('Notification has been send');
          return response;
        }).catch(error => {
          res.send(err);
        });


      } else {
        console.log('User document does not exists..');
        return null;
      }
      return null;
    }).catch(err => {
      res.send(err);
    });

  });