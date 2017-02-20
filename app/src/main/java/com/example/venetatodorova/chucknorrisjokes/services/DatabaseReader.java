package com.example.venetatodorova.chucknorrisjokes.services;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.example.venetatodorova.chucknorrisjokes.activities.MainActivity;
import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderContract;
import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderDBHelper;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DatabaseReader implements Runnable {

    private FeedReaderDBHelper dbHelper;
    private String joke;
    private Handler uiHandler;

    public DatabaseReader(Context context, Handler uiHandler) {
        dbHelper = new FeedReaderDBHelper(context);
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        joke = readFromDatabase();
        Message msg = Message.obtain();
        msg.obj = joke;
        uiHandler.sendMessage(msg);
    }

    private String readFromDatabase() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String[] projection = {
                FeedReaderContract.FeedEntry.COLUMN_ID,
                FeedReaderContract.FeedEntry.COLUMN_JOKES};
        String selection = FeedReaderContract.FeedEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = {getRandomJokeID()};
        Cursor cursor = database.query(FeedReaderContract.FeedEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            joke = cursor.getString(cursor.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_JOKES));
        }
        if (cursor != null) {
            cursor.close();
        }
        return joke;
    }

    private String getRandomJokeID() {
        int id;
        Random random = new Random();
        id = random.nextInt(getDatabaseSize()) + 1;
        return String.valueOf(id);
    }

    private int getDatabaseSize(){
        return (int) DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), FeedReaderContract.FeedEntry.TABLE_NAME);
    }
}
