package com.mauriundjens.sechsstundenapp;

import android.content.SharedPreferences;
import android.os.Bundle;
// import android.support.design.widget.FloatingActionButton;
// import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
// import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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

        clockworks[0].start();
        clockworks[1].setMillis(7200000);
        clockworks[1].start(-1);
        clockworks[2].setMillis(120000);
        clockworks[2].start(-1);

        restoreState();
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
        if (id == R.id.action_settings) {
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
