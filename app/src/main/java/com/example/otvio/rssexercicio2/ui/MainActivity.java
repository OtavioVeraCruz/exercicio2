package com.example.otvio.rssexercicio2.ui;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.otvio.rssexercicio2.R;
import com.example.otvio.rssexercicio2.db.SQLiteRSSHelper;
import com.example.otvio.rssexercicio2.domain.ItemRSS;
import com.example.otvio.rssexercicio2.util.CarregaFeedService;
import com.example.otvio.rssexercicio2.util.ParserRSS;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class MainActivity extends Activity {

    private ListView conteudoRSS;
    private final String RSS_FEED = "http://rss.cnn.com/rss/edition.rss";
    private SQLiteRSSHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = SQLiteRSSHelper.getInstance(this);

        conteudoRSS = findViewById(R.id.conteudoRSS);

        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(
                        //contexto, como estamos acostumados
                        this,
                        //Layout XML de como se parecem os itens da lista
                        R.layout.item,
                        //Objeto do tipo Cursor, com os dados retornados do banco.
                        //Como ainda não fizemos nenhuma consulta, está nulo.
                        null,
                        //Mapeamento das colunas nos IDs do XML.
                        // Os dois arrays a seguir devem ter o mesmo tamanho
                        new String[]{SQLiteRSSHelper.ITEM_TITLE, SQLiteRSSHelper.ITEM_DATE},
                        new int[]{R.id.itemTitulo, R.id.itemData},
                        //Flags para determinar comportamento do adapter, pode deixar 0.
                        0
                );
        //Seta o adapter. Como o Cursor é null, ainda não aparece nada na tela.
        conteudoRSS.setAdapter(adapter);

        // permite filtrar conteudo pelo teclado virtual
        conteudoRSS.setTextFilterEnabled(true);

        //Complete a implementação deste método de forma que ao clicar, o link seja aberto no navegador e
        // a notícia seja marcada como lida no banco
        conteudoRSS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
                Cursor mCursor = ((Cursor) adapter.getItem(position));
                String link=mCursor.getString(4);

                  if (db.markAsRead(link)){
                    Intent intent = new Intent(getApplicationContext(),WebActivity.class);
                    intent.putExtra("link",link);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Não foi possível realizar a ação!"
                            ,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String linkfeed = preferences.getString("rssfeedlink", getResources().getString(R.string.rssfeed));
       // new CarregaRSS().execute(linkfeed);
        Intent load_feed=new Intent(this,CarregaFeedService.class);
        load_feed.putExtra("link",linkfeed);
        startService(load_feed);
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter(CarregaFeedService.FEED_LOADED);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(onDownloadCompleteEvent, f);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_Config:
                startActivity(new Intent(this, ConfigActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class CarregaRSS extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... feeds) {
            boolean flag_problema = false;
            List<ItemRSS> items = null;
            try {
                String feed = MainActivity.getRssFeed(feeds[0]);
                items = ParserRSS.parse(feed);
                for (ItemRSS i : items) {
                    Log.d("DB", "Buscando no Banco por link: " + i.getLink());
                    ItemRSS item = db.getItemRSS(i.getLink());
                    if (item == null) {
                        Log.d("DB", "Encontrado pela primeira vez: " + i.getTitle());
                        db.insertItem(i);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                flag_problema = true;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                flag_problema = true;
            }
            return flag_problema;
        }

        @Override
        protected void onPostExecute(Boolean teveProblema) {
            if (teveProblema) {
                Toast.makeText(getApplicationContext(), "Houve algum problema ao carregar o feed.", Toast.LENGTH_SHORT).show();
            } else {
                //dispara o task que exibe a lista
                new MainActivity.ExibirFeed().execute();
            }
        }
    }

    class ExibirFeed extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            Cursor c = db.getItems();
            c.getCount();
            return c;
        }

        @Override
        protected void onPostExecute(Cursor c) {
            if (c != null) {
                ((CursorAdapter) conteudoRSS.getAdapter()).changeCursor(c);
            }
        }
    }

    public static String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }

    private BroadcastReceiver onDownloadCompleteEvent=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent i) {
            Toast.makeText(ctxt, "Feed carregado com sucesso!", Toast.LENGTH_LONG).show();
            new MainActivity.ExibirFeed().execute();
        }
    };

}
