package br.com.gpaengenharia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewFlipper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import br.com.gpaengenharia.R;
import br.com.gpaengenharia.beans.Projeto;
import br.com.gpaengenharia.beans.Tarefa;
import br.com.gpaengenharia.classes.AdaptadorProjetos;
import br.com.gpaengenharia.classes.AdaptadorTarefas;
import br.com.gpaengenharia.classes.Notificacao;
import br.com.gpaengenharia.classes.Utils;
import br.com.gpaengenharia.classes.provedorDados.ProvedorDados;
import br.com.gpaengenharia.classes.provedorDados.ProvedorDadosTarefasArquivadas;
import br.com.gpaengenharia.classes.provedorDados.ProvedorDadosTarefasEquipe;
import br.com.gpaengenharia.classes.provedorDados.ProvedorDadosTarefasHoje;
import br.com.gpaengenharia.classes.provedorDados.ProvedorDadosTarefasPessoais;
import br.com.gpaengenharia.classes.provedorDados.ProvedorDadosTarefasSemana;

/**
 * Base Activity for all users
 * list the personal tasks with option to change to team tasks, today tasks and week
 * also option for group tasks by project or single tasks
 */
public abstract class AtvBase extends Activity implements OnGroupClickListener, OnChildClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AtvLogin.usuario == null)
            startActivityIfNeeded(new Intent(this, AtvLogin.class), 0);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (AtvLogin.usuario == null)
            startActivityIfNeeded(new Intent(this, AtvLogin.class), 0);
    }

    /**
     * flip the layout from left to right
     * @return null, because we cannot return to prior activity 
     */
    private ViewFlipper viewFlipper; //flip layouts
    @Override
    public Intent getParentActivityIntent() {
        Utils.deslizaLayoutEsquerda(this.viewFlipper, findViewById(R.id.LayoutDashboard));
        return null;
    }

    /**
     * set the common views for Administrator and Colaborator, called in onCreate event
     */;
    private ExpandableListView lvProjetos;//expandable listView with projects containing tasks
    public static ProgressBar prgTarefas;
    protected void setViews(){
        if (AtvLogin.usuario != null) {
            //Log.i("onCreate", String.valueOf(atualizaListView));
            this.viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
            this.lvProjetos = (ExpandableListView) findViewById(R.id.LVprojetos);
            this.lvProjetos.setGroupIndicator(null);
            this.lvProjetos.setOnGroupClickListener(this);
            this.lvProjetos.setOnChildClickListener(this);
            //I could not put just one progressbar for both :(
            if (AtvLogin.usuario.getPerfil().equals("adm"))
                this.prgTarefas = (ProgressBar) findViewById(R.id.PRGtarefasAdm);
            else
                this.prgTarefas = (ProgressBar) findViewById(R.id.PRGtarefasColaborador);
            //listView starts with personal tasks
            this.projetosPessoais(false);
        }
    }

    /**
     * if user not logged in go back to activity login
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (AtvLogin.usuario == null)
            startActivityIfNeeded(new Intent(this, AtvLogin.class), 0);
        else
            this.atualizaListView();
    }

    /**
     * if the activity is foreground, verify if have to update
     * the tasks case the ServicoTarefas (or another) class has set the flag 'atualizaListView'
     */
    @Override
    public void onUserInteraction() {
        //Log.i("onUserInteraction", String.valueOf(atualizaListView));
        this.atualizaListView();
    }

    /**
     * update the tasks case the ServicoTarefas (or another) class has set the flag 'atualizaListView'
     */
    //flag setted by ServicoTarefas class, indicates that have to update
    public static boolean atualizaListView;
    //---------------------------------------------------------------------------------------------
    private void atualizaListView(){
        if (atualizaListView){
            this.zeraObjetos();
            this.projetosPessoais(false);
            Notificacao.cancell(this,1);
            atualizaListView = false;
        }
    }

    /**
     * passing the work for flip the screen to Utils class
     * @param event
     * @return false, if onTouch event was captured, true inverse
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Utils.contexto = this;
        return Utils.onTouchEvent(  event,
                                    this.viewFlipper,
                                    findViewById(R.id.LayoutDashboard),
                                    findViewById(R.id.LayoutTarefas));
    }

    /**
     * returns the tree map of personal projects inverted:
     * task list containing sublist of personal projects
     */
    //polimorfic instance that gives personal, team, today and week data projects
    public static ProvedorDados provedorDados;
    // <Projeto, List<Tarefa>> tree map of personal projects contaning task sublist
    private TreeMap<Projeto, List<Tarefa>> projetosTreeMap;
    // <Tarefa, List<Projeto>> inverted tree map 
    private TreeMap<Tarefa, List<Projeto>> tarefasTreeMap;
    private char agrupamento = 't';//t=broup by tasks, p=group by projects
    //---------------------------------------------------------------------------------------------
    private void agrupaTarefas(){
        if (this.projetosTreeMap == null || this.projetosTreeMap.isEmpty())
            this.projetosTreeMap = this.provedorDados.getTreeMapBeanProjetosTarefas();
        if (this.tarefasTreeMap == null || this.tarefasTreeMap.isEmpty()) {
            this.tarefasTreeMap = new TreeMap<Tarefa, List<Projeto>>();
            /**generates new TreeMap inverted with task and List<Projeto>
             * TODO encapsulate on provedorDados class*/
            for (Map.Entry<Projeto, List<Tarefa>> projetoTarefas : this.projetosTreeMap.entrySet()){
                List<Projeto> projetos = new ArrayList<Projeto>();
                projetos.add(projetoTarefas.getKey());
                for (Tarefa tarefa : projetoTarefas.getValue())
                    this.tarefasTreeMap.put(tarefa, projetos);
            }
        }
        this.setAdaptador(true);
        //short the distance between every group on ExpandableListView (COP=Cat Oriented Programming)     :)
        this.lvProjetos.setDividerHeight(-20);
        this.agrupamento = 't';
    }

    /**
     * returns the default tree map:
     * project list with task sublist
     */
    private void agrupaProjetos(){
        if (this.projetosTreeMap == null || this.projetosTreeMap.isEmpty())
            this.projetosTreeMap = this.provedorDados.getTreeMapBeanProjetosTarefas();
        this.setAdaptador(false);
        this.lvProjetos.setDividerHeight(0);
        this.agrupamento = 'p';
    }

    /**
     * adapt the personal projects on expandable listView
     * @param inverte true = TreeMap <Tarefa,ArrayList<Projeto>>
     *                false = TreeMap <Projeto, ArrayList<Tarefa>>
     */
    private AdaptadorProjetos adaptadorProjetos; //project adapter to listView
    private AdaptadorTarefas adaptadorTarefas; //task adapter to listView
    //---------------------------------------------------------------------------------------------
    private void setAdaptador(boolean inverte){
        if (inverte) {
            //singleton
            if (!(this.adaptadorTarefas instanceof AdaptadorTarefas))
                this.adaptadorTarefas = new AdaptadorTarefas(this, this.tarefasTreeMap);
            this.lvProjetos.setAdapter(this.adaptadorTarefas);
        }else {
            //singleton
            if (!(this.adaptadorProjetos instanceof AdaptadorProjetos))
                this.adaptadorProjetos = new AdaptadorProjetos(this, this.projetosTreeMap);
            this.lvProjetos.setAdapter(this.adaptadorProjetos);
        }
        //if adapter is empty, alert
        if (this.lvProjetos.getAdapter().isEmpty())
            Toast.makeText(this,"nenhuma tarefa",Toast.LENGTH_LONG).show();
    }

    /**inflate the commom menu to Administrator and Colaborator
     * @param menu
     * @return MenuInflater to add more options
     */
    public MenuInflater criaMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_base, menu);
        return inflater;
    }

    /**commom menu options for Admistrator and Colaborator
     * @param item
     * @return the selected menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.actionbar_novoprojeto:
            case R.id.menu_novoprojeto:
                startActivity(new Intent(AtvBase.this, AtvProjeto.class));
                break;
            case R.id.novatarefa:
            case R.id.menu_novatarefa:
                startActivity(new Intent(AtvBase.this, AtvTarefa.class));
                break;
            case R.id.projetos_pessoais:
                this.projetosPessoais(false);
                break;
            case R.id.projetos_equipe:
                this.projetosEquipes(false);
                break;
            case R.id.projetos_hoje:
                this.projetosHoje(false);
                break;
            case R.id.projetos_semana:
                this.projetosSemana(false);
                break;
            case R.id.agrupamento_tarefa:
            case R.id.actionbar_tarefa:
                agrupaTarefas();
                break;
            case R.id.agrupamento_projeto:
            case R.id.actionbar_projeto:
                agrupaProjetos();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * TODO maybe it will be better have one ProvedorDados class for every kind of task instead of one polimorfic for all...
     * to not reintantiate, explaining: if I tap the personal tasks, then team tasks and then personal again it will...
     * reinstantiate personal task twice (I have to think about it more)
     */
    //override methods used by menu above and view layout_base
    public void projetosPessoais(View v){
        Utils.deslizaLayoutDireita(this.viewFlipper, findViewById(R.id.LayoutTarefas));
        this.projetosPessoais(false);
    }

    public void projetosPessoais(final boolean forcarAtualizacao){
        //singleton
        if (!(this.provedorDados instanceof ProvedorDadosTarefasPessoais)) {
            this.zeraObjetos();
            TarefasTask tarefasTask = new TarefasTask();
            tarefasTask.execute('p');
        }else
            this.agrupaTarefas();
    }

    public void projetosEquipes(View v){
        Utils.deslizaLayoutDireita(this.viewFlipper, findViewById(R.id.LayoutTarefas));
        this.projetosEquipes(false);
    }

    public void projetosEquipes(final boolean forcarAtualizacao){
        //singleton
        if (!(this.provedorDados instanceof ProvedorDadosTarefasEquipe)) {
            this.zeraObjetos();
            TarefasTask tarefasTask = new TarefasTask();
            tarefasTask.execute('e');
        }else
            this.agrupaTarefas();
    }

    public void projetosHoje(View v){
        Utils.deslizaLayoutDireita(this.viewFlipper, findViewById(R.id.LayoutTarefas));
        this.projetosHoje(false);
    }

    public void projetosHoje(final boolean forcarAtualizacao){
        //singleton
        if (!(this.provedorDados instanceof ProvedorDadosTarefasHoje)) {
            this.zeraObjetos();
            TarefasTask tarefasTask = new TarefasTask();
            tarefasTask.execute('h');
        }else
            this.agrupaTarefas();
    }

    public void projetosSemana(View v){
        Utils.deslizaLayoutDireita(this.viewFlipper, findViewById(R.id.LayoutTarefas));
        this.projetosSemana(false);
    }

    public void projetosSemana(final boolean forcarAtualizacao){
        //singleton
        if (!(this.provedorDados instanceof ProvedorDadosTarefasSemana)) {
            this.zeraObjetos();
            TarefasTask tarefasTask = new TarefasTask();
            tarefasTask.execute('s');
        }else
            this.agrupaTarefas();
    }

    public void tarefasArquivadas(View v){
        Utils.deslizaLayoutDireita(this.viewFlipper, findViewById(R.id.LayoutTarefas));
        this.tarefasArquivadas(false);
    }

    public void tarefasArquivadas(final boolean forcarAtualizacao){
        //singleton
        if (!(this.provedorDados instanceof ProvedorDadosTarefasArquivadas)) {
            this.zeraObjetos();
            TarefasTask tarefasTask = new TarefasTask();
            tarefasTask.execute('a');
        }else
            this.agrupaTarefas();
    }

    //used by above methods projetosPessoais, projetosEquipes, etc
    private void zeraObjetos(){
        this.projetosTreeMap = null;
        this.tarefasTreeMap = null;
        this.adaptadorTarefas = null;
        this.adaptadorProjetos = null;
    }

    /**
     * search the tasks by foreground webservice
     */
    public class TarefasTask extends AsyncTask<Character, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            Utils.barraProgresso(AtvBase.this, prgTarefas, true);
        }
        @Override
        protected Boolean doInBackground(Character... provedorDados) {
            if (AtvLogin.usuario != null) {
                switch (provedorDados[0]) {
                    case 'p':
                        AtvBase.setProvedorDados(new ProvedorDadosTarefasPessoais(AtvBase.this, false));
                        break;
                    case 'e':
                        AtvBase.setProvedorDados(new ProvedorDadosTarefasEquipe(AtvBase.this, false));
                        break;
                    case 'h':
                        AtvBase.setProvedorDados(new ProvedorDadosTarefasHoje(AtvBase.this, false));
                        break;
                    case 's':
                        AtvBase.setProvedorDados(new ProvedorDadosTarefasSemana(AtvBase.this, false));
                        break;
                    case 'a':
                        AtvBase.setProvedorDados(new ProvedorDadosTarefasArquivadas(AtvBase.this, false));
                        break;
                }
                return true;
            }else
                return false;
        }
        @Override
        protected void onPostExecute(Boolean resultado) {
            Utils.barraProgresso(AtvBase.this, prgTarefas, false);
            if (resultado)
                agrupaTarefas();
        }
    }

    /**
     * set the data provider for tasks
     * @param provedorDados
     */
    public static void setProvedorDados(ProvedorDados provedorDados){
        AtvBase.provedorDados = provedorDados;
    }

    /**when tap the ExpandableListView calls AtvTarefa Activity and pass
     * the beans Projeto and Tarefa
     * @param parent
     * @param v
     * @param groupPosition
     * @param id
     * @return
     */
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        if (this.agrupamento == 't') {
            Tarefa tarefa = (Tarefa) parent.getExpandableListAdapter().getGroup(groupPosition);
            this.atualizaTarefaTreeMap(tarefa.getId());
            //send the object Tarefa parcelable to activity atvTarefa
            Bundle bundleTarefa = new Bundle();
            bundleTarefa.putParcelable("tarefa", tarefa);
            bundleTarefa.putParcelable("projeto", (Projeto) parent.getExpandableListAdapter().getChild(groupPosition, 0));
            Intent atvTarefa = new Intent(AtvBase.this, AtvTarefa.class);
            atvTarefa.putExtras(bundleTarefa);
            startActivity(atvTarefa);
            return true;
        }else
            return false;
    }

    /**when tap a sub item from ExpandableListView calls Activity AtvTarefa and pass
     * the beans Projeto and Tarefa
     * @param parent
     * @param v
     * @param groupPosition
     * @param childPosition
     * @param id
     * @return
     */
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (this.agrupamento == 'p') {
            Tarefa tarefa = (Tarefa) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
            this.atualizaTarefaTreeMap(tarefa.getId());
            Bundle bundleTarefa = new Bundle();
            bundleTarefa.putParcelable("projeto", (Projeto) parent.getExpandableListAdapter().getGroup(groupPosition));
            bundleTarefa.putParcelable("tarefa", tarefa);
            Intent atvTarefa = new Intent(AtvBase.this, AtvTarefa.class);
            atvTarefa.putExtras(bundleTarefa);
            startActivity(atvTarefa);
            return true;
        }else
            return false;
    }

    /**update tarefasTreeMap case a task has been updated by Activity AtvTarefa
     * @param idTarefa
     */
    // flag sent by Activity AtvTarefa to know what task update on TreeMap
    public static int atualizarTarefaId;
    private void atualizaTarefaTreeMap(int idTarefa){
        //if task was tapped check with the task updated by activity AtvTarefa...
        if (idTarefa == atualizarTarefaId) { //...so rebuild TreeMap
            this.provedorDados = null;
            this.projetosPessoais(false);
            atualizarTarefaId = 0; //flag TreeMap already updated
        }
    }

}
