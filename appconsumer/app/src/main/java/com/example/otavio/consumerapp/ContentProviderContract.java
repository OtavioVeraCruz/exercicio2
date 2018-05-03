package com.example.otavio.consumerapp;

import android.content.ContentResolver;
import android.net.Uri;

class ContentProviderContract {
    static String _ID="_id";
    static String TITLE="title";
    static String DATE="date";
    static String DESCRIPTION="description";
    static String LINK="link";
    static String UNREAD="unread";
    public static final String TABLE_ITEMS = "items";
    public static final Uri FEED_CONTENT_URI =Uri.parse("content://br.ufpe.cin.if1001.rss.leitura/");

    private static final Uri BASE_RSS_URI = Uri.parse("content://com.example.otvio.rssexercicio2/");

    public static final Uri LIST_URI = Uri.withAppendedPath(FEED_CONTENT_URI, TABLE_ITEMS);

    public static final String DIR_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/RssProvider.data.text";

    public static final String ITEM_CONTENT_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/RssProvider.data.text";
}
