package com.example.otvio.rssexercicio2.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.otvio.rssexercicio2.R;
import com.example.otvio.rssexercicio2.ui.MainActivity;

public class UpdateFeedReceiver extends BroadcastReceiver {
    private final String TAG = "UpdateFeedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1=new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext()
                , 0, intent1, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext())
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle("RSS Feed")
                .setContentText("Não fique desatualizado!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification notification=mBuilder.build();
        NotificationManagerCompat.from(context.getApplicationContext()).notify(0,notification);
    }
}
