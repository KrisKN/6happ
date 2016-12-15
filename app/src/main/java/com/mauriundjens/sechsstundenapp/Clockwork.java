package com.mauriundjens.sechsstundenapp;

import java.io.Serializable;

public class Clockwork implements Serializable
{
    private long offsetMillis = 0; // offset when timer was started
    private long startMillis; // time of start
    private double speed = 0.0; // speed multiplier

    public Clockwork()
    {
    }

    private long calcMillis(long timeMillis)
    {
        long result = offsetMillis + Math.round((timeMillis - startMillis) * speed);
        if (result < 0) return 0;
        if (result > 21600000) return 21600000; // todo: maximum should be settable
        return result;
    }

    public void update(long timeMillis)
    {
        offsetMillis = calcMillis(timeMillis);
        startMillis = timeMillis;
    }

    public void update()
    {
        // time elapsed since last update is recorded,
        // must be called before parameter changes
        long currentMillis = System.currentTimeMillis();
        update(currentMillis);
    }

    public long getSystemTimeAt(long millis)
    {
        // calculates the system time when the given clockwork value is reached
        if (speed == 0.0) return -1;
        return startMillis + Math.round((millis - offsetMillis) / speed);
    }

    public void start(double speed)
    {
        update();
        this.speed = speed;
    }

    public void start()
    {
        update();
        speed = 1;
    }

    public void stop()
    {
        update();
        speed = 0;
    }

    public void reset()
    {
        speed = 0;
        offsetMillis = 0;
    }

    public void resetTo(long millis)
    {
        speed = 0;
        offsetMillis = millis;
    }

    public void setSpeed(double value)
    {
        update();
        speed = value;
    }

    public double getSpeed()
    {
        return speed;
    }

    public void setMillis(long millis)
    {
        offsetMillis = millis;
        startMillis = System.currentTimeMillis();
    }

    public long getCurrentMillis()
    {
        // return ms at current system time
        return calcMillis(System.currentTimeMillis());
    }

    public long getStartMillis()
    {
        // return time of last start or update
        return startMillis;
    }

    public long getUpdateMillis()
    {
        // return ms at last start or update
        return offsetMillis;
    }

    public String toString()
    {
        long millis = getCurrentMillis();
        long s = (millis / 1000) % 60;
        long m = (millis / 60000) % 60;
        long h = millis / 3600000;
        return String.format("%d:%02d:%02d", h, m, s);
    }
}
