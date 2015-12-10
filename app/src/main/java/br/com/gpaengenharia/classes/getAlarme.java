package br.com.gpaengenharia.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import br.com.gpaengenharia.activities.AtvBase;
import br.com.gpaengenharia.activities.AtvLogin;

/**
 * create the background service responsible for sync the tasks
 */
public class getAlarme extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.barraProgresso(context, AtvBase.prgTarefas, true);
        if (AtvLogin.usuario != null)
            context.startService(new Intent(context, ServicoTarefas.class));
    }
}
