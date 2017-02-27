package com.example.venetatodorova.chucknorrisjokes.services;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderContract;
import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderDBHelper;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DatabaseReader {

    private FeedReaderDBHelper dbHelper;
    private String joke;
    private DatabaseReaderListener listener;
    private Disposable disposable;

    public DatabaseReader(Context context, DatabaseReaderListener listener) {
        dbHelper = new FeedReaderDBHelper(context);
        this.listener = listener;
    }

    public void startReading() {
        disposable = Observable.interval(0, 3, TimeUnit.SECONDS)
                .flatMap(ignored -> Observable.fromCallable(this::readFromDatabase))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(s -> listener.onJokeRead(s));
    }

    public void stopReader() {
        disposable.dispose();
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
        Log.i("DatabaseReader", "read from DB");
        return joke;
    }

    private String getRandomJokeID() {
        int id;
        Random random = new Random();
        id = random.nextInt(getDatabaseSize()) + 1;
        return String.valueOf(id);
    }

    private int getDatabaseSize() {
        return (int) DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), FeedReaderContract.FeedEntry.TABLE_NAME);
    }

    public interface DatabaseReaderListener {
        void onJokeRead(String joke);
    }
}
