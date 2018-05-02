package com.example.otvio.rssexercicio2.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.example.otvio.rssexercicio2.R;

public class WebActivity extends AppCompatActivity {

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        webView=(WebView)findViewById(R.id.web);
        Intent intent=getIntent();
        String link = intent.getStringExtra("link");//"https://www.globo.com";
        webView.loadUrl(link);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
