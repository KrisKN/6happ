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

    public void schedule(final Context context, final long timeInMillis, final int id, final String text, final Notification notification) {
        // create alarm (and pass notification, which is shown at this time)
        Intent intent = new Intent(context, AlarmScheduler.class);
        intent.putExtra("id", id);
        intent.putExtra("text", text);
        intent.putExtra("notification", notification);
        PendingIntent operation = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, operation);
    }

    public void cancel(final Context context, final int id) {
        // cancel previous alarm
        Intent intent = new Intent(context, AlarmScheduler.class);
        PendingIntent operation = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(operation);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // retrieve info
        int id = intent.getIntExtra("id", 0);
        String text = intent.getStringExtra("text");

        // retrieve notification created earlier
        Notification notification = intent.getParcelableExtra("notification");

        // show notification
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);

        // show toast
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

}
