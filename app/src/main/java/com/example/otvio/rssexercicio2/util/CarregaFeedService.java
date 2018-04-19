package com.example.otvio.rssexercicio2.util;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.otvio.rssexercicio2.db.SQLiteRSSHelper;
import com.example.otvio.rssexercicio2.domain.ItemRSS;
import com.example.otvio.rssexercicio2.ui.MainActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

//Service para carregar na main activity os links
public class CarregaFeedService extends IntentService {
    private SQLiteRSSHelper db = SQLiteRSSHelper.getInstance(this);

    public static final String FEED_LOADED = "com.example.otvio.rssexercicio2.ui.action.FEED_LOADED";
    public static final String INSERTED_DATA = "com.example.otvio.rssexercicio2.ui.action.INSERTED_DATA";
    public CarregaFeedService() {
        super("CarregaFeedService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        String feeds=intent.getStringExtra("link");
        List<ItemRSS> items;
        try {
            String feed = MainActivity.getRssFeed(feeds);
            items = ParserRSS.parse(feed);
            for (ItemRSS i : items) {
                Log.d("DB", "Buscando no Banco por link: " + i.getLink());
                ItemRSS item = db.getItemRSS(i.getLink());
                if (item == null) {
                    sendBroadcast(new Intent(INSERTED_DATA));
                    Log.d("DB", "Encontrado pela primeira vez: " + i.getTitle());
                    db.insertItem(i);

                }
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(FEED_LOADED));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        finally {
            db.close();
        }
    }
}
