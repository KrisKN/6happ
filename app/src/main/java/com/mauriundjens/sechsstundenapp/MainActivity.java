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

    private View[] views = new View[4];
    private TextView[] textViews = new TextView[4];
    private ImageView[] imageViews = new ImageView[4];

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
        textViews[0] = (TextView)findViewById(R.id.textView_ornella);
        textViews[1] = (TextView)findViewById(R.id.textView_maurizio);
        textViews[2] = (TextView)findViewById(R.id.textView_jens);
        textViews[3] = (TextView)findViewById(R.id.textView_julia);
        imageViews[0] = (ImageView)findViewById(R.id.icon_ornella);
        imageViews[1] = (ImageView)findViewById(R.id.icon_maurizio);
        imageViews[2] = (ImageView)findViewById(R.id.icon_jens);
        imageViews[3] = (ImageView)findViewById(R.id.icon_julia);

        // initialize clocks
        resetTimersToSixHours();
        restoreState();
        updateIcons();

        /*
        for(Clockwork clockwork : clockworks) {
            clockwork.start(-1);
        }
        */

        // call handleTimer each second
        timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                handleTimer();
            }
        }, 0, 1000);

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
            updateIcons();
            return true;
        }
        else if (id == R.id.action_accelerate_countdown) {

            for(Clockwork clockwork : clockworks) {
                clockwork.setSpeed(-3);
            }
            updateIcons();
            return true;
        }
        else if (id == R.id.action_count_up) {

            for(Clockwork clockwork : clockworks) {
                clockwork.setSpeed(1);
            }
            updateIcons();
        }
        else if (id == R.id.action_to_normal_speed) {

            for(Clockwork clockwork : clockworks) {
                clockwork.setSpeed(-1);
            }
            updateIcons();
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

    private void resetTimersToSixHours() {

        for(Clockwork clockwork : clockworks) {
            // reset to 6h
            clockwork.resetTo(21600000);
        }
    }

    private View.OnClickListener createOnClickListener(final int index) {

        final ImageView icon = imageViews[index];

        return new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                if (clockworks[index].getSpeed() == 0) {
                    clockworks[index].setSpeed(-1);
                }
                else {
                    clockworks[index].setSpeed(0);
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
        final ImageView icon = imageViews[index];
        if (clockwork.getSpeed() == 0) {
            icon.setImageResource(android.R.drawable.ic_media_pause);
        }
        else {
            icon.setImageResource(android.R.drawable.ic_media_play);
        }
    }

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
