package com.example.eightpuzzle;

import android.os.Handler;
import android.os.Looper;

public class GameTimer {

    public interface TimerCallback {
        void onTick(int seconds);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int secondsElapsed = 0;
    private boolean running = false;
    private TimerCallback callback;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                secondsElapsed++;
                if (callback != null) {
                    callback.onTick(secondsElapsed);
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    public void start(TimerCallback callback) {
        this.callback = callback;
        this.secondsElapsed = 0;
        this.running = true;
        handler.postDelayed(timerRunnable, 1000);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(timerRunnable);
    }

    public void reset() {
        stop();
        secondsElapsed = 0;
    }

    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}