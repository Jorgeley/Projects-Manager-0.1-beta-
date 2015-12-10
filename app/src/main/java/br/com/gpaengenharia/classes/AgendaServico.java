package br.com.gpaengenharia.classes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

/**
 * Executed on Android boot and set the sync
 * of tasks by 10 to 10 minutes
 */
public class AgendaServico extends BroadcastReceiver {
    private final static int intervaloSincronismo = 60000; //miliseconds: 1minute

    @Override
    public void onReceive(Context context, Intent intent) {
        //define the moment to shot the sync
        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(System.currentTimeMillis()); //get the current time
        calendario.add(Calendar.MINUTE, 1); //1 minute after now
        //alarm that shot the sync
        AlarmManager alarme = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long tempo = calendario.getTimeInMillis();
        //intent to receive the alarm
        Intent intentAlarme = new Intent("EXECUTA_ALARME"); //seted on AndroidManifest
        PendingIntent intentPendente = PendingIntent.getBroadcast(context, 0, intentAlarme, 0);
        //schedule the sync to 1 minute from now, repeating by 10 to 10 minutes
        alarme.setRepeating(AlarmManager.RTC_WAKEUP,
                            tempo, //execute alarm after 10 minutes from now
                            intervaloSincronismo , //repeat by 10 to 10 minutes
                            intentPendente //intent that receives the alarm
        );
    }

}
