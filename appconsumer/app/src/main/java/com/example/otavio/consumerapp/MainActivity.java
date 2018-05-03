package com.example.otavio.consumerapp;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listview);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new CarregaFeed().execute();
        //consulta na main thread, pode ser custoso, usar AsyncTask ou Loader


    }
    class CarregaFeed extends AsyncTask<Void,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Void... voids) {
            ContentResolver cr = getContentResolver();
            return cr.query(ContentProviderContract.LIST_URI, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if (cursor != null) {
                ((CursorAdapter) listView.getAdapter()).changeCursor(cursor);
            }
        }


    }

}
