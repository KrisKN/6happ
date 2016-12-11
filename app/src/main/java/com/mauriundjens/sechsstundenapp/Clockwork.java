package com.mauriundjens.sechsstundenapp;

public class Clockwork
{
    private long offsetMillis = 0;
    private long startMillis;
    private int speed = 0;

    public Clockwork()
    {
    }

    private long calcMillis(long timeMillis)
    {
        long result = offsetMillis + (timeMillis - startMillis) * speed;
        return result < 0 ? 0 : result;
    }

    private void update()
    {
        // time elapsed since last update is recorded,
        // must be called before parameter changes or serialization
        long currentMillis = System.currentTimeMillis();
        offsetMillis = calcMillis(currentMillis);
        startMillis = currentMillis;
    }

    public void start(int speed)
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
        setMillis(millis);
    }

    public void setSpeed(int value)
    {
        update();
        speed = value;
    }

    public int getSpeed()
    {
        return speed;
    }

    public void setMillis(long millis)
    {
        offsetMillis = millis;
        startMillis = System.currentTimeMillis();
    }

    public long getMillis()
    {
        return calcMillis(System.currentTimeMillis());
    }

    public String toString()
    {
        long millis = getMillis();
        long s = (millis / 1000) % 60;
        long m = (millis / 60000) % 60;
        long h = millis / 3600000;
        return String.format("%d:%02d:%02d", h, m, s);
    }
}
