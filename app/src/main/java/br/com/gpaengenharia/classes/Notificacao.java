package br.com.gpaengenharia.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Create notifications to updated tasks
 */
public class Notificacao {

    /**
     * Create notifications according the correct Android API
     * @param contexto
     * @param titulo
     * @param mensagem
     * @param icone
     * @param id
     * @param intent
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static void create(Context contexto, CharSequence titulo, CharSequence mensagem, int icone, int id, Intent intent){
        //intent to open when tap the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(contexto, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notificacao = null;
        int api = Build.VERSION.SDK_INT; //API level
        if (api >= 11) {
            Builder builder = new Builder(contexto)
                    .setContentTitle(titulo)
                    .setContentText(mensagem)
                    .setSmallIcon(icone)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            if (api >= 17) //android 4.2
                notificacao = builder.build();
            else //android 3.x
                notificacao = builder.getNotification();
        }else{ //android 2.2
            notificacao = new Notification(icone, titulo, System.currentTimeMillis());
            notificacao.setLatestEventInfo(contexto, titulo, mensagem, pendingIntent);
        }
        NotificationManager notificacaoManager = (NotificationManager) contexto.getSystemService(Activity.NOTIFICATION_SERVICE);
        notificacaoManager.notify(id, notificacao);//id: identify the notification
    }

    /**
     * Cancel the notification by its id
     * @param contexto
     * @param id
     */
    public static void cancell(Context contexto, int id){
        NotificationManager notificacaoManager = (NotificationManager) contexto.getSystemService(Activity.NOTIFICATION_SERVICE);
        notificacaoManager.cancel(id);
    }

}
