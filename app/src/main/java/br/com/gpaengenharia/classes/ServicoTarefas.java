package br.com.gpaengenharia.classes;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import br.com.gpaengenharia.R;
import br.com.gpaengenharia.activities.AtvBase;
import br.com.gpaengenharia.activities.AtvLogin;
import br.com.gpaengenharia.activities.AtvTarefa;
import br.com.gpaengenharia.beans.Projeto;
import br.com.gpaengenharia.beans.Tarefa;
import br.com.gpaengenharia.classes.xmls.Xml;
import br.com.gpaengenharia.classes.xmls.XmlTarefasArquivadas;
import br.com.gpaengenharia.classes.xmls.XmlTarefasEquipe;
import br.com.gpaengenharia.classes.xmls.XmlTarefasHoje;
import br.com.gpaengenharia.classes.xmls.XmlTarefasPessoais;
import br.com.gpaengenharia.classes.xmls.XmlTarefasSemana;

/**
 * service scheduled by AgendaServico class to be executed every minute
 * verify by webservice if there were updates on tasks, if yes, download the
 * updated XML and create notification for every updated task
 */
public class ServicoTarefas extends Service implements Runnable{
    private Context contexto;
    private boolean notificacoes;

    public ServicoTarefas() {
        this.notificacoes = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; //no interactions
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (AtvLogin.usuario != null)
            new Thread(this).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run(){
        //XML file containing the updated tasks
        File arquivo = new File(this.getContexto().getFilesDir() + "/" + Xml.getNomeArquivoXMLatualizadas());
        SimpleDateFormat formatoData = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", new Locale("pt", "BR"));
        Date data = new Date();
        data.setTime(arquivo.lastModified());//get the modification time of XML file
        String ultimaSincronizacao = formatoData.format(data);
        Vector<Vector<Object>> respostasSincroniza;//ids of tasks updateds and flags of wich tasks to update
        TreeMap<Projeto, List<Tarefa>> projetosTarefas; //treeMap of project beans containing tasks beans in everyone
        try {
            Xml xml = new Xml(this.getContexto());
            //calls the webservice that verify if there are new tasks checking the modification time of XML file
            respostasSincroniza = xml.sincronizaXmlTudoWebservice(AtvLogin.usuario, ultimaSincronizacao);
            if (respostasSincroniza != null) {
                if (this.getNotificacoes()) {
                    //build the treeMap of beans
                    projetosTarefas = xml.leXmlProjetosTarefas(respostasSincroniza.get(0)); //index 0 contains ids of updated tasks
                    if (!projetosTarefas.isEmpty()) {
                        //Log.i("projetosTarefas", String.valueOf(projetosTarefas));
                        for (Map.Entry<Projeto, List<Tarefa>> projetoTarefas : projetosTarefas.entrySet()) {
                            //for every new task create a notification
                            for (Tarefa tarefa : projetoTarefas.getValue()) {
                                if (!tarefa.getStatus().equals("arquivada")) {
                                    Bundle bundleTarefa = new Bundle();
                                    bundleTarefa.putParcelable("projeto", projetoTarefas.getKey());
                                    bundleTarefa.putParcelable("tarefa", tarefa);
                                    Intent atvTarefa = new Intent(this.getContexto(), AtvTarefa.class);
                                    atvTarefa.putExtras(bundleTarefa);
                                    String msg = null;
                                    switch (tarefa.getStatus()) {
                                        case "aberta":
                                            msg = " [atualizada]";
                                            break;
                                        case "concluir":
                                            msg = " [confirmar conclusao?]";
                                            break;
                                        case "rejeitada":
                                            msg = " [rejeitada conclusao!]";
                                            break;
                                        case "concluida":
                                            msg = " [concluida!]";
                                            break;
                                        case "excluir":
                                            msg = " [excluida!]";
                                            //atvTarefa = null;
                                            break;
                                    }
                                    Notificacao.create(this.getContexto(),
                                            "GPA",
                                            tarefa.getNome() + msg,
                                            R.drawable.logo_notificacao,
                                            tarefa.getId(), //if equals the extras don't update it
                                            atvTarefa
                                    );
                                    Date hoje = new Date();
                                    Long diferenca = tarefa.getVencimento().getTime() - hoje.getTime();
                                    Log.i("diferença", String.valueOf(diferenca));
                                    if (diferenca <= 432000000){ //5*24*60*60*1000 = 432000000 miliseconds (5 days)
                                        SimpleDateFormat formatoDataDiferenca = new SimpleDateFormat("dd", new Locale("pt", "BR"));
                                        Notificacao.create(this.getContexto(),
                                                "GPA",
                                                "-"+formatoDataDiferenca.format(diferenca)+" dias p/ vencimento: "+tarefa.getNome(),
                                                R.drawable.logo_notificacao,
                                                tarefa.getId(), //if equals the extras don't update it
                                                atvTarefa
                                        );
                                    }
                                    //flag to activity atvBase update the view
                                    AtvBase.atualizaListView = true;
                                }
                            }
                        }
                    }
                }
                //update the XML files according the flags sent by webservice
                if (respostasSincroniza.get(1) != null) {
                    //indice [1][0] contains flag to sync XML personal tasks
                    Boolean sincronizaPessoais = (Boolean) respostasSincroniza.get(1).get(0);
                    //indice [1][1] contains flag to sync XML team tasks
                    Boolean sincronizaEquipes = (Boolean) respostasSincroniza.get(1).get(1);
                    //indice [1][2] contains flag to sync XML today tasks
                    Boolean sincronizaHoje = (Boolean) respostasSincroniza.get(1).get(2);
                    //indice [1][3] contains flag to sync XML week tasks
                    Boolean sincronizaSemana = (Boolean) respostasSincroniza.get(1).get(3);
                    //indice [1][4] contains flag to sync XML filed tasks
                    Boolean sincronizaArquivadas = (Boolean) respostasSincroniza.get(1).get(4);
                    if (sincronizaPessoais) {
                        XmlTarefasPessoais xmlTarefasPessoais = new XmlTarefasPessoais(this.getContexto());
                        xmlTarefasPessoais.criaXmlProjetosPessoaisWebservice(AtvLogin.usuario, true);
                    }
                    if (sincronizaEquipes) {
                        XmlTarefasEquipe xmlTarefasEquipe = new XmlTarefasEquipe(this.getContexto());
                        xmlTarefasEquipe.criaXmlProjetosEquipesWebservice(AtvLogin.usuario, true);
                    }
                    if (sincronizaHoje) {
                        XmlTarefasHoje xmlTarefasHoje = new XmlTarefasHoje(this.getContexto());
                        xmlTarefasHoje.criaXmlProjetosHojeWebservice(AtvLogin.usuario, true);
                    }
                    if (sincronizaSemana) {
                        XmlTarefasSemana xmlTarefasSemana = new XmlTarefasSemana(this.getContexto());
                        xmlTarefasSemana.criaXmlProjetosSemanaWebservice(AtvLogin.usuario, true);
                    }
                    if (sincronizaArquivadas) {
                        XmlTarefasArquivadas xmlTarefasArquivadas = new XmlTarefasArquivadas(this.getContexto());
                        xmlTarefasArquivadas.criaXmlTarefasArquivadasWebservice(AtvLogin.usuario, true);
                    }
                }
            }
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        //because God wants this way, do not ask me why!!!
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run(){
                if (AtvBase.prgTarefas != null)
                    AtvBase.prgTarefas.setVisibility(View.GONE);
            }
        });
    }

    public void setContexto(Context contexto){
        this.contexto = contexto;
    }

    public Context getContexto(){
        if (this.contexto == null)
            return getApplicationContext();
        else
            return this.contexto;
    }

    public void setNotificacoes(boolean notificacoes) {
        this.notificacoes = notificacoes;
    }

    public boolean getNotificacoes() {
        return this.notificacoes;
    }
}
