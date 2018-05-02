package com.example.otvio.rssexercicio2.ui;
import android.app.AlarmManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.otvio.rssexercicio2.R;
import com.example.otvio.rssexercicio2.db.SQLiteRSSHelper;
import com.example.otvio.rssexercicio2.domain.ItemRSS;
import com.example.otvio.rssexercicio2.util.CarregaFeedService;
import com.example.otvio.rssexercicio2.util.JobSchedulerDownload;
import com.example.otvio.rssexercicio2.util.ParserRSS;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ListView conteudoRSS;
    private SQLiteRSSHelper db;
    public static final String KEY_DOWNLOAD="isDownload";
    JobScheduler jobScheduler;
    private static final int JOB_ID = 710;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        jobScheduler=(JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String linkfeed = preferences.getString("rssfeedlink", null);
        Log.d("Link",linkfeed);
        Intent load_feed=new Intent(this,CarregaFeedService.class);
        load_feed.putExtra("link",linkfeed);
        startService(load_feed);
        agendarJob();
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter(CarregaFeedService.FEED_LOADED);
        LocalBroadcastManager.getInstance(getApplicationContext()).
                registerReceiver(onDownloadCompleteEvent, f);
    }
    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).
                unregisterReceiver(onDownloadCompleteEvent);
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void agendarJob() {
        JobInfo.Builder b = new JobInfo.Builder(JOB_ID, new ComponentName(this,
                JobSchedulerDownload.class));
        PersistableBundle pb=new PersistableBundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            pb.putBoolean(KEY_DOWNLOAD, true);
        }
        b.setExtras(pb);

        //criterio de rede
        b.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        //b.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);

        //define intervalo de periodicidade
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String period=preferences.getString("rsstime",null);
        long three_hours=3*AlarmManager.INTERVAL_HOUR;
        long six_hours=6*AlarmManager.INTERVAL_HOUR;
        long [] periods={AlarmManager.INTERVAL_HALF_HOUR,AlarmManager.INTERVAL_HOUR,
        three_hours,six_hours,AlarmManager.INTERVAL_HALF_DAY,AlarmManager.INTERVAL_DAY};
        assert period != null;
        switch (period){
           case "30 min":
               b.setPeriodic(periods[0]);
               break;
           case "1h":
               b.setPeriodic(periods[1]);
               break;
           case "3h":
               b.setPeriodic(periods[2]);
               break;
           case "6h":
               b.setPeriodic(periods[3]);
               break;
           case "12h":
               b.setPeriodic(periods[4]);
               break;
           case "24h":
               b.setPeriodic(periods[5]);
               break;

       }
        //b.setPeriodic(getPeriod());

        //exige (ou nao) que esteja conectado ao carregador
        b.setRequiresCharging(false);

        //persiste (ou nao) job entre reboots
        //se colocar true, tem que solicitar permissao action_boot_completed
        b.setPersisted(false);

        //exige (ou nao) que dispositivo esteja idle
        b.setRequiresDeviceIdle(false);

        //backoff criteria (linear ou exponencial)
        //b.setBackoffCriteria(1500, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        //periodo de tempo minimo pra rodar
        //so pode ser chamado se nao definir setPeriodic...
        //b.setMinimumLatency(3000);

        //mesmo que criterios nao sejam atingidos, define um limite de tempo
        //so pode ser chamado se nao definir setPeriodic...
      //  b.setOverrideDeadline(6000);

        jobScheduler.schedule(b.build());
    }
}