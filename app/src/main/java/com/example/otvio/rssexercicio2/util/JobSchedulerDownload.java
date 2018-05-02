package com.example.otvio.rssexercicio2.util;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.otvio.rssexercicio2.R;
import com.example.otvio.rssexercicio2.ui.MainActivity;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerDownload extends JobService {

    //Essa classe é responsável por executar o jobscheduler
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String downloadLink = preferences.getString("rssfeedlink", null);
        PersistableBundle pb=jobParameters.getExtras();

        if (pb.getBoolean(MainActivity.KEY_DOWNLOAD, false)) {
            Intent downloadService = new Intent (getApplicationContext(),CarregaFeedService.class);
            downloadService.putExtra("link",downloadLink);
            getApplicationContext().startService(downloadService);
            Log.d("Entrou no jobScheduler","YEP");
            return true;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Intent downloadService = new Intent (getApplicationContext(),CarregaFeedService.class);
        getApplicationContext().stopService(downloadService);
        return true;
    }
}
