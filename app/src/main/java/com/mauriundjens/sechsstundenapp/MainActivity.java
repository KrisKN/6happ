package com.mauriundjens.sechsstundenapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MainActivity extends AppCompatActivity {

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
    }

    private void changeSpeed(double speed) {
        boolean noClockworkActive = isNoClockworkActive();
        for(Clockwork clockwork : clockworks) {
            if (noClockworkActive || clockwork.getSpeed() < 0.0) clockwork.setSpeed(speed);
        }
        updateIcons();
        updateAlarm();
    }

    private View.OnClickListener createOnClickListener(final int index) {
        final ImageView icon = icons[index];
        return new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                if (clockworks[index].getSpeed() >= 0.0) {
                    clockworks[index].setSpeed(-1.0);
                    updateAlarm();
                }
                else {
                    clockworks[index].setSpeed(1.0);
                    updateAlarm();
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

    private int findNextAlarmIndex(long millis) {
        int result = -1;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        for (int i = 0; i < 4; ++i) {
            long time = clockworks[i].getSystemTimeAt(millis);
            if (time > now && time < minTime) {
                // better one found
                result = i;
                minTime = time;
            }
        }
        return result;
    }

    private void updateAlarm() {
        final long oneHourMillis = 3600000;
        if (giftCounter == 0)
        {
            scheduleAlarm(oneHourMillis * 6 - 30000); // after 30 sec
        }
        else if (giftCounter == 1)
        {
            scheduleAlarm(oneHourMillis * 4);
        }
        else if (giftCounter == 2)
        {
            scheduleAlarm(oneHourMillis * 3);
        }
        else if (giftCounter == 3)
        {
            scheduleAlarm(0);
        }
    }

    private void scheduleAlarm(long millis) {
        // cancel any pending events
        scheduler.cancel(this);

        int nextAlarmIndex = findNextAlarmIndex(millis);
        if (nextAlarmIndex >= 0) {
            long time = clockworks[nextAlarmIndex].getSystemTimeAt(millis);
            scheduler.schedule(this, time, R.mipmap.ic_launcher);
        }
    }
}
