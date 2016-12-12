package com.mauriundjens.sechsstundenapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


public class AlarmScheduler extends BroadcastReceiver {

    public AlarmScheduler() {
    }

    public void schedule(Context context, Bundle extras, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmScheduler.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);

        // todo: die folgende Nachricht ist nur zu Debugzwecken und muss noch weg
        Toast.makeText(context, "gleich geht's los", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "es ist so weit", Toast.LENGTH_LONG).show();
        // todo: die Zeit ist abgelaufen, was passiert hier? Gutschein???
    }

}
