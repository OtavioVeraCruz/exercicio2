package com.example.otvio.rssexercicio2.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.example.otvio.rssexercicio2.db.SQLiteRSSHelper;



public class CarregaFeedService extends Service {
    private SQLiteRSSHelper db = SQLiteRSSHelper.getInstance(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
