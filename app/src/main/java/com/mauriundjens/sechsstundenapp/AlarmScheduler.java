package com.mauriundjens.sechsstundenapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class AlarmScheduler extends BroadcastReceiver {

    // class must have parameterless constructor!
    public AlarmScheduler() {
    }

    public void schedule(Context context, long timeInMillis, int id, Notification notification) {
        // create alarm (and pass notification, which is shown at this time)
        Intent intent = new Intent(context, AlarmScheduler.class);
        intent.putExtra("id", id);
        intent.putExtra("notification", notification);
        PendingIntent operation = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, operation);
    }

    public void cancel(Context context, int id) {
        // cancel previous alarm
        Intent intent = new Intent(context, AlarmScheduler.class);
        PendingIntent operation = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(operation);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // show toast
        Toast.makeText(context, "Wer hat an der Uhr gedreht?", Toast.LENGTH_LONG).show();

        // retrieve notification created earlier
        int id = intent.getIntExtra("id", 0);
        Notification notification = intent.getParcelableExtra("notification");

        // show notification
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

}
