package com.mauriundjens.sechsstundenapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final long oneHourMillis = 3600000;

    private java.util.Timer timer;
    private Clockwork[] clockworks = new Clockwork[]{new Clockwork(), new Clockwork(), new Clockwork(), new Clockwork()};
    private AlarmScheduler scheduler = new AlarmScheduler();

    private View[] views = new View[4];
    private ImageView[] images = new ImageView[4];
    private TextView[] textViews = new TextView[4];
    private ImageView[] icons = new ImageView[4];
    private Button[] buttons = new Button[4];

    private int giftCounter = 0;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // find GUI elements
        views[0] = findViewById(R.id.grid_ornella);
        views[1] = findViewById(R.id.grid_maurizio);
        views[2] = findViewById(R.id.grid_jens);
        views[3] = findViewById(R.id.grid_julia);
        images[0] = (ImageView)findViewById(R.id.image_ornella);
        images[1] = (ImageView)findViewById(R.id.image_maurizio);
        images[2] = (ImageView)findViewById(R.id.image_jens);
        images[3] = (ImageView)findViewById(R.id.image_julia);
        textViews[0] = (TextView)findViewById(R.id.textView_ornella);
        textViews[1] = (TextView)findViewById(R.id.textView_maurizio);
        textViews[2] = (TextView)findViewById(R.id.textView_jens);
        textViews[3] = (TextView)findViewById(R.id.textView_julia);
        icons[0] = (ImageView)findViewById(R.id.icon_ornella);
        icons[1] = (ImageView)findViewById(R.id.icon_maurizio);
        icons[2] = (ImageView)findViewById(R.id.icon_jens);
        icons[3] = (ImageView)findViewById(R.id.icon_julia);
        buttons[0] = (Button)findViewById(R.id.giftButton1);
        buttons[1] = (Button)findViewById(R.id.giftButton2);
        buttons[2] = (Button)findViewById(R.id.giftButton3);
        buttons[3] = (Button)findViewById(R.id.giftButton4);

        // initialize clocks
        resetTimersToSixHours();
        restoreState();
        updateIcons();
        updateGifts();
        updateAlarm();

        /*
        for(Clockwork clockwork : clockworks) {
            clockwork.start(-1);
        }
        */

        // call handleTimer periodically
        timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                handleTimer();
            }
        }, 0, 500);

        // register action handlers for play/pause actions
        for (int i = 0; i < 4; ++i)
        {
            views[i].setOnClickListener(createClockClickListener(i));
        }

        // register action handlers for gift buttons
        for (int i = 0; i < 4; ++i)
        {
            buttons[i].setOnClickListener(createGiftClickListener(i));
        }
    }

    @Override
    protected void onPause() {
        saveState();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reset_timers) {
            resetTimersToSixHours();
            return true;
        }
        else if (id == R.id.action_accelerate_countdown) {
            changeSpeed(-2.0);
            return true;
        }
        else if (id == R.id.action_half_speed) {
            changeSpeed(-0.5);
            return true;
        }
        else if (id == R.id.action_normal_speed) {
            changeSpeed(-1.0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleTimer()
    {
        // is executed in context of timer thread
        runOnUiThread(timerHandler);
    }

    private Runnable timerHandler = new Runnable() {
        @Override
        public void run() {
            // is executed in context of GUI thread
            updateTexts();
            checkAlarm();
        }
    };

    private void updateTexts() {
        for (int i = 0; i < 4; ++i) {
            textViews[i].setText(clockworks[i].toString());
        }
    }

    private boolean isNoClockworkActive() {
        for(Clockwork clockwork : clockworks) {
            if (clockwork.getSpeed() < 0.0) return false;
        }
        return true;
    }

    private void resetTimersToSixHours() {
        for(Clockwork clockwork : clockworks) {
            // reset to 6h
            clockwork.resetTo(21600000);
        }
        updateIcons();
        updateTexts();
        updateAlarm();
        checkAlarm();
    }

    private void changeSpeed(double speed) {
        boolean noClockworkActive = isNoClockworkActive();
        for(Clockwork clockwork : clockworks) {
            if (noClockworkActive || clockwork.getSpeed() < 0.0) clockwork.setSpeed(speed);
        }
        updateIcons();
        updateTexts();
        updateAlarm();
        checkAlarm();
    }

    private View.OnClickListener createClockClickListener(final int index) {
        return new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                if (clockworks[index].getSpeed() >= 0.0) {
                    clockworks[index].setSpeed(-1.0);
                    updateAlarm();
                    checkAlarm();
                }
                else {
                    clockworks[index].setSpeed(1.0);
                    updateAlarm();
                    checkAlarm();
                }
                updateIcon(index);
            }
        };
    }

    private View.OnClickListener createGiftClickListener(final int index) {
        return new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                showGift(index);
            }
        };
    }

    private void updateIcons() {
        for (int i = 0; i < 4; ++i) {
            updateIcon(i);
        }
    }

    private void updateIcon(final int index)
    {
        final Clockwork clockwork = clockworks[index];
        // final ImageView image = images[index];
        final ImageView icon = icons[index];
        final TextView textView = textViews[index];
        if (clockwork.getSpeed() >= 0.0) {
            // image.setAlpha(0.3f);
            icon.setImageResource(android.R.drawable.ic_media_pause);
            icon.setAlpha(0.3f);
            textView.setAlpha(0.3f);
        }
        else {
            // image.setAlpha(1.0f);
            icon.setImageResource(android.R.drawable.ic_media_play);
            icon.setAlpha(1.0f);
            textView.setAlpha(1.0f);
        }
    }

    private void saveState() {
        // save timers
        File clockworkFile = new File(getFilesDir(), "clockworks.srl");
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(clockworkFile))) {
            stream.writeObject(clockworks);
        }
        catch (IOException e) {
            showError(e);
        }

        // save gift state
        File giftFile = new File(getFilesDir(), "gift.srl");
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(giftFile))) {
            stream.writeInt(giftCounter);
        }
        catch (IOException e) {
            showError(e);
        }
    }

    private void restoreState() {
        // restore timers
        File clockworkFile = new File(getFilesDir(), "clockworks.srl");
        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(clockworkFile))) {
            Object potentialClockworks = stream.readObject();
            if (potentialClockworks != null) { clockworks = (Clockwork[])potentialClockworks; }
        }
        catch (IOException|ClassNotFoundException e) {
            // alright, fresh clockworks are used
        }

        // restore gift state
        File giftFile = new File(getFilesDir(), "gift.srl");
        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(giftFile))) {
            giftCounter = stream.readInt();
        }
        catch (IOException e) {
            // alright, we're starting with zero anyway
        }
    }

    private long getNextAlarmMillis() {
        return getAlarmMillis(giftCounter);
    }

    private Clockwork findAlarmClockwork(final long alarmMillis)
    {
        if (alarmMillis < 0) return null;
        for (Clockwork clockwork : clockworks) {
            if (clockwork.getUpdateMillis() > alarmMillis && clockwork.getCurrentMillis() <= alarmMillis)
            {
                // ausgeloest wird nur, wenn der Wert zuvor noch zu gross fuer einen Alarm war
                // und der neue Wert den geforderten Wert erreicht oder unterschritten hat
                return clockwork;
            }
        }
        return null;
    }

    private void checkAlarm() {
        // find next alarm time
        long alarmMillis = getNextAlarmMillis();
        if (alarmMillis < 0) return;

        // has any clockwork hit this alarm time?
        Clockwork alarmClockwork = findAlarmClockwork(alarmMillis);
        if (alarmClockwork == null) return;

        // make sure no clockwork triggers another alarm before this one
        long alarmTime = alarmClockwork.getSystemTimeAt(alarmMillis);
        for (Clockwork clockwork : clockworks) {
            clockwork.update(alarmTime);
        }

        // update and save counter to avoid multiple execution of same alarm
        ++giftCounter;
        saveState();

        // update UI
        updateGifts();

        // schedule next alarm
        updateAlarm();
        checkAlarm();
    }

    private int findNextAlarmIndex(final long millis) {
        int result = -1;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        for (int i = 0; i < 4; ++i) {
            if (clockworks[i].getSpeed() < 0.0) {
                long time = clockworks[i].getSystemTimeAt(millis);
                if (time > now && time < minTime) {
                    // better one found
                    result = i;
                    minTime = time;
                }
            }
        }
        return result;
    }

    private void updateAlarm() {
        long millis = getNextAlarmMillis();
        if (millis < 0) return;
        scheduleAlarm(millis, giftCounter);
    }

    private void scheduleAlarm(final long millis, final int id) {
        // cancel any pending events
        scheduler.cancel(this, id);

        // create new event
        int nextAlarmIndex = findNextAlarmIndex(millis);
        if (nextAlarmIndex >= 0) {
            long time = clockworks[nextAlarmIndex].getSystemTimeAt(millis);
            String text = getAlarmText(giftCounter);
            scheduler.schedule(this, time, id, text, createNotification(id, text));

            // debug message
            if (BuildConfig.DEBUG) Toast.makeText(this, "alarm #" + String.valueOf(giftCounter)+ " at " + String.valueOf(millis / 60), Toast.LENGTH_LONG).show();
        }
    }

    private Notification createNotification(final int id, final String text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent operation = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("6-Stunden-App");
        builder.setContentText(text);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setAutoCancel(true);
        builder.setContentIntent(operation);
        return builder.build();
    }

    private long getAlarmMillis(final int index) {
        // just a few seconds for debugging
        if (BuildConfig.DEBUG) return 6 * oneHourMillis - 5000;

        // 3 hours and 6 hours alternately
        return index % 2 == 0 ? 3 * oneHourMillis : 0;
    }

    private String getAlarmText(final int index)
    {
        switch (index)
        {
            case 0: return getString(R.string.alarm_3h_1);
            case 1: return getString(R.string.alarm_6h_1);
            case 2: return getString(R.string.alarm_3h_2);
            case 3: return getString(R.string.alarm_6h_2);
            case 4: return getString(R.string.alarm_3h_3);
            case 5: return getString(R.string.alarm_6h_3);
        }
        if (index % 2 == 0) return getString(R.string.alarm_3h);
        return getString(R.string.alarm_6h);
    }

    private String getGiftText(final int index) {
        switch (index) {
            case 0: return getString(R.string.gift_1);
            case 1: return getString(R.string.gift_2);
            case 2: return getString(R.string.gift_3);
            case 3: return getString(R.string.gift_4);
        }
        return "weitergehn, bitte gehen Sie weiter, hier gibt es nichts zu sehn, Sie müssen weitergehn...";
    }

    private void updateGiftButton(final Button button, final boolean visible) {
        button.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateGifts() {
        updateGiftButton((Button)findViewById(R.id.giftButton1), giftCounter >= 1);
        updateGiftButton((Button)findViewById(R.id.giftButton2), giftCounter >= 2);
        updateGiftButton((Button)findViewById(R.id.giftButton3), giftCounter >= 4);
        updateGiftButton((Button)findViewById(R.id.giftButton4), giftCounter >= 6);
    }

    private void showGift(final int index) {
        // define dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.gift_title));
        builder.setMessage(getGiftText(index));
        builder.setPositiveButton(getString(R.string.cool), new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {} });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showError(final Exception e) {
        // define dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ups");
        builder.setMessage("Da hat sich ein Fehler eingeschlichen: " + e.getMessage());
        builder.setPositiveButton(R.string.cool, new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {} });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
