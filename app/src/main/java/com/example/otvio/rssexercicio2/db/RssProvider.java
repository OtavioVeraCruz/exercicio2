package com.example.otvio.rssexercicio2.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class RssProvider extends ContentProvider {
    private SQLiteRSSHelper db;
    @Override
    public boolean onCreate() {
        db=SQLiteRSSHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        return  db.getReadableDatabase().query(SQLiteRSSHelper.DATABASE_TABLE,
                SQLiteRSSHelper.columns,
                selection, selectionArgs, null, null, null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id = db.getWritableDatabase().insert(SQLiteRSSHelper.DATABASE_TABLE, null, contentValues);
        return Uri.withAppendedPath(RssProviderContract.FEED_CONTENT_URI, Long.toString(id));
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] selectionArgs) {
        return db.getWritableDatabase().delete(SQLiteRSSHelper.DATABASE_TABLE, s, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {

        return db.getWritableDatabase().update(SQLiteRSSHelper.DATABASE_TABLE, contentValues, selection, selectionArgs);
    }
}
