package com.mauriundjens.sechsstundenapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class AlarmScheduler extends BroadcastReceiver {

    // class must have parameterless constructor!
    public AlarmScheduler() {
    }

    public void schedule(Context context, long timeInMillis) {
        Intent intent = new Intent(context, AlarmScheduler.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    public void cancel(Context context) {
        Intent intent = new Intent(context, AlarmScheduler.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "es ist so weit", Toast.LENGTH_LONG).show();
        // todo: die Zeit ist abgelaufen, was passiert hier? Gutschein???
    }

}
