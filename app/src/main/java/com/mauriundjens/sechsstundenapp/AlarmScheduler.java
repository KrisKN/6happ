package com.mauriundjens.sechsstundenapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;


public class AlarmScheduler extends BroadcastReceiver {

    // class must have parameterless constructor!
    public AlarmScheduler() {
    }

    public void schedule(Context context, long timeInMillis, int icon) {
        // create notification, which is shown later
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent notificationIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle("6-Stunden-App");
        notificationBuilder.setContentText("Geburtstagsüberraschung für Christian!");
        notificationBuilder.setSmallIcon(icon);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(notificationIntent);
        Notification notification = notificationBuilder.build();

        // create alarm (and pass notification)
        Intent schedulerIntent = new Intent(context, AlarmScheduler.class);
        schedulerIntent.putExtra("notification", notification);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, schedulerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent);
    }

    public void cancel(Context context) {
        // cancel previous alarm
        Intent schedulerIntent = new Intent(context, AlarmScheduler.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, schedulerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // show toast
        Toast.makeText(context, "Geburtstagsüberraschung!", Toast.LENGTH_LONG).show();

        // retrieve notification created earlier
        Notification notification = intent.getParcelableExtra("notification");

        // show notification
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

}
