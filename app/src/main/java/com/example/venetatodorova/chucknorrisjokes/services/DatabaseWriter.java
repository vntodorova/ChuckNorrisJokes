package com.example.venetatodorova.chucknorrisjokes.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.venetatodorova.chucknorrisjokes.activities.MainActivity;
import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderContract;
import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderDBHelper;

public class DatabaseWriter extends Thread {

    public Handler handler;
    private static FeedReaderDBHelper dbHelper;
    private static Handler uIHandler;

    public DatabaseWriter(Context context, Handler uiHandler){
        dbHelper = new FeedReaderDBHelper(context);
        uIHandler = uiHandler;
    }

    private static class WriterHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(getDatabaseSize() >= FeedReaderDBHelper.DATABASE_MAX_SIZE){
                getLooper().quit();
                uIHandler.sendEmptyMessage(MainActivity.DATABASE_IS_FULL);
            }
            String joke = (String) msg.obj;
            addToDatabase(joke);
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new WriterHandler();
        Looper.loop();
    }

    private static void addToDatabase(String joke) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_JOKES, joke);
        database.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        Log.d("Added to DB:",joke);
    }

    private static long getDatabaseSize(){
        return DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), FeedReaderContract.FeedEntry.TABLE_NAME);
    }
}
