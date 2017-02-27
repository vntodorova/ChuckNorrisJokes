package com.example.venetatodorova.chucknorrisjokes.services;

import android.os.Looper;

public class DatabaseWriter extends Thread {

    private Looper looper;

    public DatabaseWriter() {
        setName("DbWriter");
    }

    @Override
    public void run() {
        Looper.prepare();
        looper = Looper.myLooper();
        Looper.loop();
    }

    public Looper getLooper() {
        return looper;
    }
}
