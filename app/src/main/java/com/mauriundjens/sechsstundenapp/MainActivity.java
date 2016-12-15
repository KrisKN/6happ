package com.mauriundjens.sechsstundenapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
import java.io.FileNotFoundException;
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

    private int giftCounter = 0;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // find GUI elements
        views[0] = (View)findViewById(R.id.grid_ornella);
        views[1] = (View)findViewById(R.id.grid_maurizio);
        views[2] = (View)findViewById(R.id.grid_jens);
        views[3] = (View)findViewById(R.id.grid_julia);
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

        // initialize clocks
        resetTimersToSixHours();
        restoreState();
        updateIcons();
        updateGifts();

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
            views[i].setOnClickListener(createOnClickListener(i));
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
            changeSpeed(0.5);
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
            updateTextViews();
            checkAlarm();
        }
    };

    private void updateTextViews() {
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
        updateAlarm();
        checkAlarm();
    }

    private void changeSpeed(double speed) {
        boolean noClockworkActive = isNoClockworkActive();
        for(Clockwork clockwork : clockworks) {
            if (noClockworkActive || clockwork.getSpeed() < 0.0) clockwork.setSpeed(speed);
        }
        updateIcons();
        updateAlarm();
        checkAlarm();
    }

    private View.OnClickListener createOnClickListener(final int index) {
        final ImageView icon = icons[index];
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

    private void updateIcons() {
        for (int i = 0; i < 4; ++i) {
            updateIcon(i);
        }
    };

    private void updateIcon(final int index)
    {
        final Clockwork clockwork = clockworks[index];
        final ImageView image = images[index];
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
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
        }

        // save gift state
        File giftFile = new File(getFilesDir(), "gift.srl");
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(giftFile))) {
            stream.writeInt(giftCounter);
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
        }
    }

    private void restoreState() {
        // restore timers
        File clockworkFile = new File(getFilesDir(), "clockworks.srl");
        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(clockworkFile))) {
            Object potentialClockworks = stream.readObject();
            if (potentialClockworks != null) { clockworks = (Clockwork[])potentialClockworks; }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // restore gift state
        File giftFile = new File(getFilesDir(), "gift.srl");
        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(giftFile))) {
            giftCounter = stream.readInt();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getNextAlarmMillis() {
        return getAlarmMillis(giftCounter);
    }

    private Clockwork findAlarmClockwork(long alarmMillis)
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
        // todo: Funktion von irgendwo aufrufen (am besten periodisch und vor irgendwelchen Aenderungen)
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

    private int findNextAlarmIndex(long millis) {
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

    private void scheduleAlarm(long millis, int id) {
        // cancel any pending events
        scheduler.cancel(this, id);

        // create new event
        int nextAlarmIndex = findNextAlarmIndex(millis);
        if (nextAlarmIndex >= 0) {
            long time = clockworks[nextAlarmIndex].getSystemTimeAt(millis);
            String text = getAlarmText(giftCounter);
            scheduler.schedule(this, time, id, createNotification(id, text));

            // debug message
            // Toast.makeText(this, "alarm #" + String.valueOf(giftCounter)+ " at " + String.valueOf(millis / 60), Toast.LENGTH_LONG).show();
        }
    }

    private Notification createNotification(int id, String text) {
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

    private long getAlarmMillis(int index) {
        switch (index)
        {
            case 0: return oneHourMillis * 6 - 30000; // oneHourMillis * 3; // todo: erstes Geschenk testweise nach 30 sec, muss viel hoeher sein
            case 1: return oneHourMillis * 6 - 60000; // 0; // zweites Geschenk nach 6 Stunden
            case 2: return oneHourMillis * 6 - 30000; // oneHourMillis * 3; // warten bis wieder auf 3 Stunden (ohne Geschenk)
            case 3: return 0; // drittes Geschenk nach 6 Stunden
            case 4: return oneHourMillis * 3; // warten bis wieder auf 3 Stunden (ohne Geschenk)
            case 5: return 0; // viertes Geschenk nach 6 Stunden
            case 6: return oneHourMillis * 3; // warten bis wieder auf 3 Stunden (ohne Geschenk)
            case 7: return 0; // kein Geschenk mehr nach 6 Stunden (aber Nachricht)
        }
        return -1;
    }

    private String getAlarmText(int index)
    {
        switch (index)
        {
            case 0: return "3 Stunden sind rum. Dafür gibt's 'ne Belohnung!";
            case 1: return "Tatsächlich 6 Stunden ausgehalten. Dafür gibt's nochmal was...";
            case 2: return "Halbzeit! Noch 3 weitere Stunden...";
            case 3: return "Und wieder 6 Stunden. Hol Dein Geschenk ab...";
            case 4: return "3 Stunden sind erst die halbe Miete...";
            case 5: return "Nochmal 6 Stunden rum. Ob es nochmal was gibt?";
            case 6: return "Wer hat an der Uhr gedreht?";
        }
        return "Jetzt ist aber mal gut...";
    }

    private void updateGiftButton(Button button, boolean visible) {
        button.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateGifts() {
        updateGiftButton((Button)findViewById(R.id.giftButton1), giftCounter >= 1);
        updateGiftButton((Button)findViewById(R.id.giftButton2), giftCounter >= 2);
        updateGiftButton((Button)findViewById(R.id.giftButton3), giftCounter >= 4);
        updateGiftButton((Button)findViewById(R.id.giftButton4), giftCounter >= 6);
    }
}
