package com.example.venetatodorova.chucknorrisjokes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.venetatodorova.chucknorrisjokes.database.FeedReaderContract.SQL_CREATE_ENTRIES;
import static com.example.venetatodorova.chucknorrisjokes.database.FeedReaderContract.SQL_DELETE_ENTRIES;

public class FeedReaderDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ChuckNorris.db";
    private static final int DATABASE_VERSION = 1;
    public static int DATABASE_MAX_SIZE = 10;

    public FeedReaderDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
