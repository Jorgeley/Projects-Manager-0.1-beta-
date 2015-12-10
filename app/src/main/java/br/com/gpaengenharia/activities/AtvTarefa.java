package br.com.gpaengenharia.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import br.com.gpaengenharia.R;
import br.com.gpaengenharia.beans.Projeto;
import br.com.gpaengenharia.beans.Tarefa;
import br.com.gpaengenharia.beans.Usuario;
import br.com.gpaengenharia.classes.ServicoTarefas;
import br.com.gpaengenharia.classes.Utils;
import br.com.gpaengenharia.classes.Utils.DatePickerFragment;
import br.com.gpaengenharia.classes.Utils.DatePickerFragment.Listener;
import br.com.gpaengenharia.classes.WebService;
import br.com.gpaengenharia.classes.xmls.XmlProjeto;
import br.com.gpaengenharia.classes.xmls.XmlTarefasArquivadas;
import br.com.gpaengenharia.classes.xmls.XmlTarefasEquipe;
import br.com.gpaengenharia.classes.xmls.XmlTarefasHoje;
import br.com.gpaengenharia.classes.xmls.XmlTarefasPessoais;
import br.com.gpaengenharia.classes.xmls.XmlTarefasSemana;
import br.com.gpaengenharia.classes.xmls.XmlUsuario;

/**
 * Activity for manage tasks
 */
public class AtvTarefa extends FragmentActivity implements Listener, OnItemSelectedListener, OnClickListener{
    private EditText EdtTarefa;
    private EditText EdtDescricao;
    private EditText EdtDialogo;
    private EditText EdtVencimento;
    private Spinner SpnResponsavel;
    private Spinner SpnProjeto;
    private ProgressBar PrgTarefa;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AtvLogin.usuario == null)
            startActivityIfNeeded(new Intent(this, AtvLogin.class), 0);
        else{
            setContentView(R.layout.atv_tarefa);
            this.EdtTarefa = (EditText) findViewById(R.id.EDTtarefa);
            this.EdtDescricao = (EditText) findViewById(R.id.EDTdescricao);
            this.EdtDialogo = (EditText) findViewById(R.id.EDTdialogo);
            this.EdtDialogo.setMovementMethod(new ScrollingMovementMethod());
            this.EdtVencimento = (EditText) findViewById(R.id.EDTvencimento);
            this.SpnResponsavel = (Spinner) findViewById(R.id.SPNresponsavel);
            this.SpnProjeto = (Spinner) findViewById(R.id.SPNprojeto);
            this.PrgTarefa = (ProgressBar) findViewById(R.id.PRGtarefa);
            this.SpnProjeto.setOnItemSelectedListener(this);
            /**
             * bring the projects list
             */
            new AsyncTask<Void, Void, List<Projeto>>(){
                @Override
                protected List<Projeto> doInBackground(Void... voids) {
                    List<Projeto> projetos = null;
                    try {
                        XmlProjeto xmlProjeto = new XmlProjeto(AtvTarefa.this);
                        xmlProjeto.criaXmlProjetosWebservice(false);
                        projetos = xmlProjeto.leXmlProjetos();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return projetos;
                }
                @Override
                protected void onPostExecute(final List<Projeto> projetos) {
                    if (projetos != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SpnProjeto.setAdapter(new ArrayAdapter<>(AtvTarefa.this, android.R.layout.simple_spinner_item, projetos));
                                if (AtvTarefa.this.getProjeto() != null)
                                    SpnProjeto.setSelection(((ArrayAdapter) SpnProjeto.getAdapter()).getPosition(AtvTarefa.this.getProjeto()));
                            }
                        });
                    }
                }
            }.execute();
            if (this.SpnProjeto.getAdapter()!=null && this.getProjeto()!=null)
                this.SpnProjeto.setSelection(((ArrayAdapter) this.SpnProjeto.getAdapter()).getPosition(AtvTarefa.this.getProjeto()));
            if (AtvLogin.usuario.getPerfil().equals("adm") ) {
                addBotoes();//if the user is administrator, add buttons
                this.SpnResponsavel.setOnItemSelectedListener(this);
                /**
                 * bring the users list
                 */
                new AsyncTask<Void, Void, List<Usuario>>() {
                    @Override
                    protected List<Usuario> doInBackground(Void... voids) {
                        List<Usuario> usuarios = null;
                        try {
                            XmlUsuario xmlUsuario = new XmlUsuario(AtvTarefa.this);
                            xmlUsuario.criaXmlUsuariosWebservice(false);
                            usuarios = xmlUsuario.leXmlUsuarios();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return usuarios;
                    }
                    @Override
                    protected void onPostExecute(final List<Usuario> usuarios) {
                        if (usuarios != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SpnResponsavel.setAdapter(new ArrayAdapter<>(AtvTarefa.this, android.R.layout.simple_spinner_item, usuarios));
                                    if (AtvTarefa.this.getTarefa() != null)
                                        SpnResponsavel.setSelection(((ArrayAdapter) SpnResponsavel.getAdapter()).getPosition(AtvTarefa.this.getTarefa().getUsuario()));
                                }
                            });
                        }
                    }
                }.execute();
                if (this.SpnResponsavel.getAdapter()!=null && this.getTarefa()!=null)
                    this.SpnResponsavel.setSelection(((ArrayAdapter) this.SpnResponsavel.getAdapter()).getPosition(AtvTarefa.this.getTarefa().getUsuario()));
            }else
                this.SpnResponsavel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (AtvLogin.usuario == null)
            startActivityIfNeeded(new Intent(this, AtvLogin.class), 0);
    }

    @Override
    protected void onResume(){
        Bundle bundleTarefa = getIntent().getExtras();
        //if was sent Projeto and Tarefa objects, set on views
        if (bundleTarefa != null) {
            this.setProjeto((Projeto) bundleTarefa.getParcelable("projeto"));
            this.setTarefa((Tarefa) bundleTarefa.getParcelable("tarefa"));
            this.EdtTarefa.setText(this.getTarefa().getNome());
            //SpnResponsavel.setAdapter(Utils.setAdaptador(this, this.responsaveis));
            this.EdtDescricao.setText(Html.fromHtml(this.getTarefa().getDescricao()));
            if (this.getTarefa().getComentario()!=null)
                this.EdtDialogo.setText(Html.fromHtml(this.getTarefa().getComentario()));
            SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            String data = formatoData.format(this.getTarefa().getVencimento());//set date
            this.EdtVencimento.setText(data);
            if (this.getTarefa().getStatus().equals("concluir"))
                this.conclui();
        }else{//if there is not bundleTarefa so is a new task, then take off the dialog
            TableRow TrDialogo = (TableRow) findViewById(R.id.TRdialogo);
            if (TrDialogo != null)
                TrDialogo.setVisibility(View.GONE);
        }
        super.onResume();
    }

    private Projeto projeto; //bean
    public Projeto getProjeto() {
        return this.projeto;
    }

    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
    }

    private Tarefa tarefa; //bean
    public Tarefa getTarefa() {
        return this.tarefa;
    }

    public void setTarefa(Tarefa tarefa) {
        this.tarefa = tarefa;
    }

    /** add addProjeto and addResponsavel buttons to layout  */
    private void addBotoes(){
        //enable Spinners
        this.SpnProjeto.setEnabled(true);
        this.SpnResponsavel.setEnabled(true);
        //create buttons
        ImageButton BtnAddProjeto = new ImageButton(this);
        BtnAddProjeto.setImageResource(android.R.drawable.ic_menu_add);
        BtnAddProjeto.setMinimumWidth(0);
        BtnAddProjeto.setMaxWidth(5);//setLayoutParams(new TableRow.LayoutParams(100,10));
        BtnAddProjeto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AtvTarefa.this, AtvProjeto.class));
            }
        });
        ImageButton BtnAddResponsavel = new ImageButton(this);
        BtnAddResponsavel.setImageResource(android.R.drawable.ic_menu_add);
        BtnAddResponsavel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AtvTarefa.this, AtvUsuarios.class));
            }
        });
        //add to layout
        TableRow TrSpinners = (TableRow) findViewById(R.id.TRspinners);
        TrSpinners.addView(BtnAddProjeto, 1);
        TrSpinners.addView(BtnAddResponsavel, 3);
        //adapt the others views to merge the cells of TableLayout
        TableRow.LayoutParams params4 = new TableRow.LayoutParams();
        params4.span = 4;
        this.EdtTarefa.setLayoutParams(params4);
        this.EdtDescricao.setLayoutParams(params4);
        this.EdtDialogo.setLayoutParams(params4);
        TableRow.LayoutParams params2 = new TableRow.LayoutParams();
        params2.span = 2;
        this.EdtVencimento.setLayoutParams(params2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.atv_tarefa, menu);
        //new task, enable save, disable others 
        if (this.getTarefa()==null){
            menu.findItem(R.id.actionbar_grava).setVisible(true);//enable save
            menu.findItem(R.id.menu_grava).setVisible(true);//enable save
            menu.findItem(R.id.actionbar_exclui).setVisible(false);//disable delete
            menu.findItem(R.id.menu_exclui).setVisible(false);//disable delete
            menu.findItem(R.id.actionbar_comenta).setVisible(false);//disable comment
            menu.findItem(R.id.menu_comenta).setVisible(false);//disable comment
            menu.findItem(R.id.actionbar_conclui).setVisible(false);//disable done
            menu.findItem(R.id.menu_conclui).setVisible(false);//disable done
        }else if ( this.getTarefa().getUsuario()!=null
                && this.getTarefa().getProjeto()!=null) {
                //task done, disable all
                if (this.getTarefa().getStatus().equals("concluida") || this.getTarefa().getStatus().equals("arquivada")){
                    menu.findItem(R.id.actionbar_grava).setVisible(false);//disable save
                    menu.findItem(R.id.menu_grava).setVisible(false);//disable save
                    menu.findItem(R.id.actionbar_exclui).setVisible(false);//disable delete
                    menu.findItem(R.id.menu_exclui).setVisible(false);//disable delete
                    menu.findItem(R.id.actionbar_comenta).setVisible(false);//disable comment
                    menu.findItem(R.id.menu_comenta).setVisible(false);//disable comment
                    menu.findItem(R.id.actionbar_conclui).setVisible(false);//disable done
                    menu.findItem(R.id.menu_conclui).setVisible(false);//disable done
                }//if is a personal task (user owns the task and project), enable all
                else if (this.getTarefa().getProjeto().getUsuario() != null
                    && this.getTarefa().getUsuario().equals(AtvLogin.usuario)
                    && this.getTarefa().getProjeto().getUsuario().equals(AtvLogin.usuario)) {
                        menu.findItem(R.id.actionbar_grava).setVisible(true);//enable save
                        menu.findItem(R.id.menu_grava).setVisible(true);//enable save
                        menu.findItem(R.id.actionbar_exclui).setVisible(true);//disable delete
                        menu.findItem(R.id.menu_exclui).setVisible(true);//disable delete
                        menu.findItem(R.id.actionbar_comenta).setVisible(true);//enable comment
                        menu.findItem(R.id.menu_comenta).setVisible(true);//enable comment
                        menu.findItem(R.id.actionbar_conclui).setVisible(true);//enable done
                        menu.findItem(R.id.menu_conclui).setVisible(true);//enable done
                }//if user is in project team
                else if (AtvLogin.usuario.getEquipes().contains(this.getTarefa().getProjeto().getEquipe())) {
                    //if profile is administrator, enable all
                    if (AtvLogin.usuario.getPerfil() == "adm") {
                        menu.findItem(R.id.actionbar_grava).setVisible(true);//enable save
                        menu.findItem(R.id.menu_grava).setVisible(true);//enable save
                        menu.findItem(R.id.actionbar_exclui).setVisible(true);//disable delete
                        menu.findItem(R.id.menu_exclui).setVisible(true);//disable delete
                        menu.findItem(R.id.actionbar_comenta).setVisible(true);//enable comment
                        menu.findItem(R.id.menu_comenta).setVisible(true);//enable comment
                        menu.findItem(R.id.actionbar_conclui).setVisible(true);//enable done
                        menu.findItem(R.id.menu_conclui).setVisible(true);//enable done
                    } else {//if is not administrator, enable comment and done
                        menu.findItem(R.id.actionbar_grava).setVisible(false);//enable save
                        menu.findItem(R.id.menu_grava).setVisible(false);//enable save
                        menu.findItem(R.id.actionbar_exclui).setVisible(false);//disable delete
                        menu.findItem(R.id.menu_exclui).setVisible(false);//disable delete
                        menu.findItem(R.id.actionbar_comenta).setVisible(true);//enable comment
                        menu.findItem(R.id.menu_comenta).setVisible(true);//enable comment
                        menu.findItem(R.id.actionbar_conclui).setVisible(true);//enable done
                        menu.findItem(R.id.menu_conclui).setVisible(true);//enable done
                    }
                }
            }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.actionbar_comenta:
            case R.id.menu_comenta:
                this.novoComentario();
                break;
            case R.id.actionbar_grava:
            case R.id.menu_grava:
                this.grava();
                break;
            case R.id.actionbar_conclui:
            case R.id.menu_conclui:
                this.conclui();
                break;
            case R.id.actionbar_exclui:
            case R.id.menu_exclui:
                this.exclui();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * save the task by webservice
     */
    private Usuario usuario;
    private void grava(){
        String nome = ((EditText) findViewById(R.id.EDTtarefa)).getText().toString();
        String descricao = ((EditText) findViewById(R.id.EDTdescricao)).getText().toString();
        String vencimentoString = ((EditText) findViewById(R.id.EDTvencimento)).getText().toString();
        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        Date vencimento = new Date();
        try {
            vencimento = formatoData.parse(vencimentoString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Tarefa tarefa;
        if (getIntent().hasExtra("tarefa"))
            tarefa = this.tarefa;//update task
        else
            tarefa = new Tarefa(Parcel.obtain());//new task
        tarefa.setNome(nome);
        tarefa.setDescricao(descricao);
        tarefa.setVencimento(vencimento);
        //seted on 'onItemSelectedListener' mtehod
        if (this.usuario != null)
            tarefa.setUsuario(this.usuario);//delegation of task
        else
            tarefa.setUsuario(AtvLogin.usuario);//personal task
        tarefa.setProjeto(this.projeto);
        GravaTarefaTask gravaTarefaTask = new GravaTarefaTask();
        gravaTarefaTask.execute(tarefa);
    }


    /**
     * save the task in background by webservice
     */
    private class GravaTarefaTask extends AsyncTask<Tarefa, Void, Boolean>{
        @Override
        protected void onPreExecute() {
            Utils.barraProgresso(AtvTarefa.this, PrgTarefa, true);
        }
        @Override
        protected Boolean doInBackground(Tarefa... tarefa) {
            boolean ok = WebService.gravaTarefa(tarefa[0]);
            if (ok) {//saved task, update all tasks
                ServicoTarefas servicoTarefas = new ServicoTarefas();
                servicoTarefas.setNotificacoes(false);//don't create notifications
                servicoTarefas.setContexto(AtvTarefa.this);
                servicoTarefas.run();
            }
            return ok;
        }
        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                Toast.makeText(AtvTarefa.this, "Tarefa Gravada", Toast.LENGTH_SHORT).show();
                AtvBase.atualizaListView = true;
                AtvTarefa.this.finish();
            }else
                Toast.makeText(AtvTarefa.this, "Erro ao tentar gravar Tarefa", Toast.LENGTH_SHORT).show();
            Utils.barraProgresso(AtvTarefa.this, PrgTarefa, false);
        }
    }

    /**
     * ask for conclusion (colaborator) of task OR
     * task done (administrator)
     */
    private void conclui() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setMessage("Confirma conclusao?");
        alerta.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            //confirm task done
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ConcluiTarefaTask concluiTarefaTask = new ConcluiTarefaTask();
                concluiTarefaTask.execute(new Object[]{AtvTarefa.this.tarefa, "sim"});
            }
        });
        if (AtvLogin.usuario.getPerfil().equals("adm")) {
            alerta.setNegativeButton("Nao", new DialogInterface.OnClickListener() {
                //reject task done
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ConcluiTarefaTask concluiTarefaWebservice = new ConcluiTarefaTask();
                    concluiTarefaWebservice.execute(new Object[]{AtvTarefa.this.tarefa, "nao"});
                }
            });
        }
        alerta.show();
    }

    /**
     * conclude the task by foreground webservice
     */
    private class ConcluiTarefaTask extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute() {
            Utils.barraProgresso(AtvTarefa.this, PrgTarefa, true);
        }
        @Override
        protected String doInBackground(Object... tarefa) {
            WebService webService = new WebService();
            webService.setUsuario(AtvLogin.usuario);
            String resposta = webService.concluiTarefa((Tarefa)tarefa[0], (String)tarefa[1]);
            if (resposta.equals("concluida")) {
                XmlTarefasArquivadas xmlTarefasArquivadas = new XmlTarefasArquivadas(AtvTarefa.this);
                AtvBase.atualizaListView = true;
                try {
                    xmlTarefasArquivadas.criaXmlTarefasArquivadasWebservice(AtvLogin.usuario, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return resposta;
        }
        @Override
        protected void onPostExecute(String resposta) {
            if (resposta!=null) {
                switch (resposta) {
                    case "concluida":
                        Toast.makeText(AtvTarefa.this, "Tarefa concluida e arquivada!", Toast.LENGTH_SHORT).show();
                        break;
                    case "concluir":
                        Toast.makeText(AtvTarefa.this,
                                "Foi solicitada a conclusao da tarefa.\n"
                                        + "Aguarde confirmaçao do administrador.",
                                Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case "rejeitada":
                        Toast.makeText(AtvTarefa.this,
                                "O responsavel pela tarefa sera avisado da pendencia.",
                                Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
            }
            Utils.barraProgresso(AtvTarefa.this, PrgTarefa, false);
            AtvTarefa.this.finish();
        }
    }

    /**
     * delete task
     */
    private void exclui(){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setMessage("Confirma exclusao? (nao podera ser desfeito)");
        alerta.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ExcluiTarefaTask excluiTarefaTask = new ExcluiTarefaTask();
                excluiTarefaTask.execute(AtvTarefa.this.getTarefa());
            }
        });
        alerta.show();
    }

    /**
     * Class responsible by delete task
     */
    public class ExcluiTarefaTask extends AsyncTask<Tarefa, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            Utils.barraProgresso(AtvTarefa.this, PrgTarefa, true);
        }
        @Override
        protected Boolean doInBackground(Tarefa... tarefas){
            //calls webservice
            WebService webService = new WebService();
            webService.setUsuario(AtvLogin.usuario);
            final Vector<Boolean> flagsSincroniza = webService.excluiTarefa(tarefas[0]);
            /** if webservice ok, update local Xmls of all tasks
             * TODO update the users XMLs responsible by tasks*/
            if (flagsSincroniza != null) {
                //flag to activity atvBase update the listView
                AtvBase.atualizaListView = true;
                try {
                    if (flagsSincroniza.get(0)) {//update personal tasks
                        XmlTarefasPessoais xmlTarefasPessoais = new XmlTarefasPessoais(AtvTarefa.this);
                        xmlTarefasPessoais.criaXmlProjetosPessoaisWebservice(AtvLogin.usuario, true);
                    }
                    if (flagsSincroniza.get(1)) {//update team tasks
                        XmlTarefasEquipe xmlTarefasEquipe = new XmlTarefasEquipe(AtvTarefa.this);
                        xmlTarefasEquipe.criaXmlProjetosEquipesWebservice(AtvLogin.usuario, true);
                    }
                    if (flagsSincroniza.get(2)) {//update today tasks
                        XmlTarefasHoje xmlTarefasHoje = new XmlTarefasHoje(AtvTarefa.this);
                        xmlTarefasHoje.criaXmlProjetosHojeWebservice(AtvLogin.usuario, true);
                    }
                    if (flagsSincroniza.get(3)) {//update week tasks
                        XmlTarefasSemana xmlTarefasSemana = new XmlTarefasSemana(AtvTarefa.this);
                        xmlTarefasSemana.criaXmlProjetosSemanaWebservice(AtvLogin.usuario, true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }else
                return false;
        }
        @Override
        protected void onPostExecute(final Boolean successo) {
            Utils.barraProgresso(AtvTarefa.this, PrgTarefa, false);
            if (successo) {
                Toast.makeText(AtvTarefa.this, "tarefa excluida", Toast.LENGTH_SHORT).show();
                AtvTarefa.this.finish();
            }else
                Toast.makeText(AtvTarefa.this, "erro ao tentar excluir a tarefa", Toast.LENGTH_SHORT).show();
        }
    }

    //used by UIdget comment
    public void novoComentario(View v){
        this.novoComentario();
    }

    /**
     * create dialog comment
     */
    View layoutComentario = null;
    private void novoComentario(){
        LayoutInflater factory = LayoutInflater.from(this);
        this.layoutComentario = factory.inflate(R.layout.comentario, null);
        final AlertDialog.Builder comentario = new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.actionbar_comenta)
                .setView(this.layoutComentario)
                .setPositiveButton(R.string.actionbar_grava, this);
        comentario.show();
    }

    /**add comment and task by foreground webservice
     * @param dialogInterface
     * @param i
     */
    private ComentarioTask comentarioTask = null;
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        EditText EdtComentario = (EditText) this.layoutComentario.findViewById(R.id.EDTcomentario);
        String comentario = EdtComentario.getText().toString();
        this.comentarioTask = new ComentarioTask();
        this.comentarioTask.execute(comentario);
    }

    /**
     * Class responsible for add the comment in foreground
     */
    public class ComentarioTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            Utils.barraProgresso(AtvTarefa.this, PrgTarefa, true);
        }
        @Override
        protected Boolean doInBackground(String... comentario){
            //calls webservice
            WebService webService = new WebService();
            webService.setUsuario(AtvLogin.usuario);
            final Object[] resposta = webService.gravacomentario(
                    AtvTarefa.this.tarefa.getId(),
                    comentario[0]
            );
            if (resposta != null) {
                /**flag sent to activity AtvBase to update TreeMap in...
                 * case of reopen the task that checks with Id below, because there was...
                 * update of comments by Activity AtvTarefa (this)  */
                AtvBase.atualizarTarefaId = AtvTarefa.this.tarefa.getId();
                Vector<Boolean> flagsSincroniza = (Vector<Boolean>) resposta[1];
                try { //save local Xml updated by webservice
                    if (flagsSincroniza != null) {
                        if (flagsSincroniza.get(0)) {//update personal tasks
                            XmlTarefasPessoais xmlTarefasPessoais = new XmlTarefasPessoais(AtvTarefa.this);
                            xmlTarefasPessoais.criaXmlProjetosPessoaisWebservice(AtvLogin.usuario, true);
                        }
                        if (flagsSincroniza.get(1)) {//update team tasks
                            XmlTarefasEquipe xmlTarefasEquipe = new XmlTarefasEquipe(AtvTarefa.this);
                            xmlTarefasEquipe.criaXmlProjetosEquipesWebservice(AtvLogin.usuario, true);
                        }
                        if (flagsSincroniza.get(2)) {//update today tasks
                            XmlTarefasHoje xmlTarefasHoje = new XmlTarefasHoje(AtvTarefa.this);
                            xmlTarefasHoje.criaXmlProjetosHojeWebservice(AtvLogin.usuario, true);
                        }
                        if (flagsSincroniza.get(3)) {//update week tasks
                            XmlTarefasSemana xmlTarefasSemana = new XmlTarefasSemana(AtvTarefa.this);
                            xmlTarefasSemana.criaXmlProjetosSemanaWebservice(AtvLogin.usuario, true);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //necessary for update EdtDialogo with the comment add
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String comentarioString = (String) resposta[0];
                        Spanned comentario = Html.fromHtml(EdtDialogo.getText()+comentarioString+"\n");
                        EdtDialogo.setText(comentario);
                    }
                });
                return true;
            }else
                return false;
        }
        @Override
        protected void onPostExecute(final Boolean successo) {
            comentarioTask = null;
            Utils.barraProgresso(AtvTarefa.this, PrgTarefa, false);
            if (successo) {
                Toast.makeText(AtvTarefa.this, "comentario gravado", Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(AtvTarefa.this, "nao foi possivel gravar o comentario", Toast.LENGTH_SHORT).show();
        }
    }

    /**returns the date of datePicker
     * @param data
     */
    @Override
    public void getData(String data) {
        this.EdtVencimento.setText(data);
    }

    /** setted diretly on OnClick property of EDTvencimento */
    public void mostraDatePicker(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(this.getFragmentManager(), "datePicker");
    }

    //used by Spinners SpnProjeto and SpnResponsavel
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getSelectedItem() instanceof Usuario)
            this.usuario = (Usuario) parent.getSelectedItem();
        else if (parent.getSelectedItem() instanceof Projeto)
            this.projeto = (Projeto) parent.getSelectedItem();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

}
