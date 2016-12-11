package com.mauriundjens.sechsstundenapp;

import android.os.Bundle;
// import android.support.design.widget.FloatingActionButton;
// import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
// import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

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

    private Map<Integer,Integer> clockworkToIcon = new HashMap<Integer, Integer>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // call handleTimer each second
        timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                handleTimer();
            }
        }, 0, 1000);

        resetTimersToSixHours();
        restoreState();

        /*
        for(Clockwork clockwork : clockworks) {
            clockwork.start(-1);
        }
        */

        clockworkToIcon.put(0, R.id.icon_ornella);
        clockworkToIcon.put(1, R.id.icon_maurizio);
        clockworkToIcon.put(2, R.id.icon_jens);
        clockworkToIcon.put(3, R.id.icon_julia);

        // register action handlers for play/pause actions
        ImageView view = (ImageView)findViewById(R.id.image_ornella);
        view.setOnClickListener(createOnClickListener(0));

        view = (ImageView)findViewById(R.id.image_maurizio);
        view.setOnClickListener(createOnClickListener(1));

        view = (ImageView)findViewById(R.id.image_jens);
        view.setOnClickListener(createOnClickListener(2));

        view = (ImageView)findViewById(R.id.image_julia);
        view.setOnClickListener(createOnClickListener(3));
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
            runOnUiThread(iconUpdateRunnable);
            return true;
        }
        else if (id == R.id.action_accelerate_countdown) {

            for(Clockwork clockwork : clockworks) {
                clockwork.setSpeed(-3);
            }
            runOnUiThread(iconUpdateRunnable);
            return true;
        }
        else if (id == R.id.action_count_up) {

            for(Clockwork clockwork : clockworks) {
                clockwork.setSpeed(1);
            }
            runOnUiThread(iconUpdateRunnable);
        }
        else if (id == R.id.action_to_normal_speed) {

            for(Clockwork clockwork : clockworks) {
                clockwork.setSpeed(-1);
            }
            runOnUiThread(iconUpdateRunnable);
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
            TextView view1 = (TextView)findViewById(R.id.textView1);
            view1.setText(clockworks[0].toString());
            TextView view2 = (TextView)findViewById(R.id.textView2);
            view2.setText(clockworks[1].toString());
            TextView view3 = (TextView)findViewById(R.id.textView3);
            view3.setText(clockworks[2].toString());
            TextView view4 = (TextView)findViewById(R.id.textView4);
            view4.setText(clockworks[3].toString());
        }
    };

    private void resetTimersToSixHours() {

        for(Clockwork clockwork : clockworks) {
            // reset to 6h
            clockwork.resetTo(21600000);
        }
    }

    private View.OnClickListener createOnClickListener(final int clockworkIndex) {

        final ImageView icon = (ImageView)findViewById(clockworkToIcon.get(clockworkIndex));

        return new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                if (clockworks[clockworkIndex].getSpeed() == 0) {
                    clockworks[clockworkIndex].setSpeed(-1);
                    icon.setImageResource(android.R.drawable.ic_media_pause);
                }
                else {
                    clockworks[clockworkIndex].setSpeed(0);
                    icon.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        };
    }


    private Runnable iconUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            for (Clockwork clockwork : clockworks) {

                final ImageView icon = (ImageView)findViewById(clockworkToIcon.get(i));
                if (clockwork.getSpeed() == 0) {
                    icon.setImageResource(android.R.drawable.ic_media_play);
                }
                else {
                    icon.setImageResource(android.R.drawable.ic_media_pause);
                }
                i++;
            }
        }
    };


    private void saveState() {
        File file = new File(getFilesDir(), "preferences.srl");
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
            stream.writeObject(clockworks);
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
        }
    }

    private void restoreState() {
        File file = new File(getFilesDir(), "preferences.srl");
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
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
    }
}
