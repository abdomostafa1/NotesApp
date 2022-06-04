package com.example.notes;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class NoteProvider extends ContentProvider {
    private SQLiteDatabase sqLiteDatabase;
    public  static String providerConstPath;
    final static String scheme="content://";
    final static String authority="com.example.notes";
    @Override
    public boolean onCreate() {
            SQLiteHelper sqLiteHelper=new SQLiteHelper(getContext(),"NotesDataBase",null,1);
            providerConstPath=scheme+authority+"/"+Table.Table_Name;
            // use inner class called AsyncThread to get reference to SQLiteDatabase
        AsyncThread asyncThread=new AsyncThread();
        asyncThread.execute(sqLiteHelper);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // newest notes first ;
        sortOrder=Table.COLUMN_ID+" DESC";
        String tableName=uri.getLastPathSegment();
      Cursor cursor= sqLiteDatabase.query(tableName,projection,selection,selectionArgs,null,null,sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String tableName=uri.getLastPathSegment();
        sqLiteDatabase.insert(tableName,null,values);
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName=uri.getLastPathSegment();
        sqLiteDatabase.delete(tableName,selection,null);
        return 1;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName=uri.getLastPathSegment();
        sqLiteDatabase.update(tableName,values,selection,null);
        return 0;
    }

    class AsyncThread extends AsyncTask<SQLiteHelper,Void,Void> {
        @SuppressLint("Range")
        @Override
        protected Void doInBackground(SQLiteHelper... sqLiteHelpers) {

            sqLiteDatabase=sqLiteHelpers[0].getWritableDatabase();
            return null;
        }
    }
}
