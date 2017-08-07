package com.chatting.makrandpawar.WildFire;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.makrandpawar.chatla.R;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTyppe = remoteMessage.getData().get("notificationType");

        if (notificationTyppe.equals("friendRequest")){
            displayFriendRequestNotification(remoteMessage);
        }else if(notificationTyppe.equals("welcomeUser")){
            displayWelcomeUserNotification(remoteMessage);
        }

    }

    private void displayWelcomeUserNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String content = remoteMessage.getData().get("body");

        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.applogo);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.applogo)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSound(sound)
                        .setAutoCancel(true)
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setLargeIcon(icon)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content).setBigContentTitle(title));

        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void displayFriendRequestNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String content = remoteMessage.getData().get("body");
        String intentAction = remoteMessage.getData().get("click_action");
        String userId = remoteMessage.getData().get("userId");
        String userImage = remoteMessage.getData().get("userImage");

        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.applogo);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.applogo)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSound(sound)
                        .setLargeIcon(icon)
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setAutoCancel(true);

        try {
            URL url = new URL(userImage);
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image).setSummaryText(content).setBigContentTitle(title));
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        Intent resultIntent = new Intent(intentAction);
        resultIntent.putExtra("VIEWPROFILE_USERREF",userId);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
