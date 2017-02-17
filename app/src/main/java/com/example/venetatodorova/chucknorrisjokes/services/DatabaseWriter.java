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

    private static FeedReaderDBHelper dbHelper;
    private static Handler uIHandler;
    private DatabaseWriterListener listener;

    public DatabaseWriter(Context context, Handler uiHandler, DatabaseWriterListener listener){
        dbHelper = new FeedReaderDBHelper(context);
        uIHandler = uiHandler;
        this.listener = listener;
    }

    @Override
    public void run() {
        Looper.prepare();
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String joke = (String) msg.obj;
                addToDatabase(joke);
                if (getDatabaseSize() >= FeedReaderDBHelper.DATABASE_MAX_SIZE) {
                    getLooper().quit();
                    uIHandler.sendEmptyMessage(MainActivity.DATABASE_IS_FULL);
                    Log.d("Writer", "full database");
                }
            }
        };
        listener.onDatabaseWriterStart(handler);
        Looper.loop();
    }

    private static void addToDatabase(String joke) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_JOKES, joke);
        database.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        Log.d("Added to DB:",joke);
        Log.d("Database size:",String.valueOf(getDatabaseSize()));
    }

    private static long getDatabaseSize(){
        return DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), FeedReaderContract.FeedEntry.TABLE_NAME);
    }

    public interface DatabaseWriterListener{
        void onDatabaseWriterStart(Handler handler);
    }
}
