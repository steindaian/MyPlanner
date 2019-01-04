package upt.myplanner;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;

import upt.myplanner.friends.Requests;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = "MessagingService";
    private String reqIds = null;

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        if(FirebaseAuth.getInstance()!=null && FirebaseAuth.getInstance().getCurrentUser()!=null)
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update("token",token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData().get("requests"));
            reqIds = remoteMessage.getData().get("requests");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    scheduleJob();
                }
            }).start();
        }
    }

    private void scheduleJob() {
        if(reqIds!=null) {
            final String[] messageBody = {""};
            final boolean[] done = {false};
            final String[] ids = reqIds.split(",");
            for(int i=0;i<ids.length;i++) {
                final int finalI = i;
                FirebaseFirestore.getInstance().collection("users").document(ids[i]).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            messageBody[0] = task.getResult().get("name") + " , " +messageBody[0];
                        }
                        if(finalI ==ids.length-1) {
                            if(messageBody[0].length()>3) messageBody[0] = messageBody[0].substring(0,messageBody[0].length()-3);
                            messageBody[0] += " sent you a friend request. Tap to view";
                            sendNotification(messageBody[0]);
                        }
                    }
                });
            }
        }
    }
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, Requests.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "default")
                        .setSmallIcon(R.drawable.calendar_img)
                        .setContentTitle(getString(R.string.request_notification))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
