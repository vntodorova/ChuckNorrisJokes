package com.example.venetatodorova.chucknorrisjokes.services;

import android.content.Context;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderContract;
import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderDBHelper;
import com.example.venetatodorova.chucknorrisjokes.views.CountdownView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DownloadThread extends Thread {

    private static final String API_URL = "http://api.icndb.com/jokes/random?escape=javascript";
    private CountdownView countdown;
    private Handler writerHandler;
    private boolean isRunning;
    private FeedReaderDBHelper dbHelper;

    public DownloadThread(Handler writerHandler, CountdownView countdown, Context context) {
        this.writerHandler = writerHandler;
        this.countdown = countdown;
        this.isRunning = true;
        dbHelper = new FeedReaderDBHelper(context);
    }

    @Override
    public void run() {
        if(getDatabaseSize() >= FeedReaderDBHelper.DATABASE_MAX_SIZE){
            isRunning = false;
        }
        while (isRunning) {
            try {
                Random random = new Random();
                int timer = random.nextInt(10) + 1;
                countdown.start(timer);
                sleep(TimeUnit.SECONDS.toMillis(timer));
                sendJokeToWriter(getRandomJoke());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendJokeToWriter(String joke) {
        Message msg = Message.obtain();
        msg.obj = joke;
        writerHandler.sendMessage(msg);
    }

    private String getRandomJoke() {
        String joke = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(API_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            JSONObject jsonObj = new JSONObject(stringBuilder.toString());
            JSONObject value = jsonObj.getJSONObject("value");
            joke = value.getString("joke");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return joke;
    }

    private long getDatabaseSize(){
        return DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), FeedReaderContract.FeedEntry.TABLE_NAME);
    }
}
