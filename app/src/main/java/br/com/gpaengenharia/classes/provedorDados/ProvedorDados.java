package br.com.gpaengenharia.classes.provedorDados;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import br.com.gpaengenharia.beans.Projeto;
import br.com.gpaengenharia.beans.Tarefa;

/**
 * Provide the data beans Projeto and Tarefa
 */
public abstract class ProvedorDados{
    protected TreeMap<Projeto, List<Tarefa>> projetosTreeMapBean = new TreeMap<Projeto, List<Tarefa>>();

    /**generates static TreeMapTeste of personal projects containing tasks
     * @return TreeMap String, ListString
     */
    public TreeMap<String, List<String>> getDadosTreeMapTeste() {
        TreeMap<String, List<String>> projetosTreeMap = new TreeMap<String, List<String>>();
        //create ArrayList of tasks
        List<String> terraplanagem = new ArrayList<String>();
        List<String> asfaltamento = new ArrayList<String>();
        List<String> condominio = new ArrayList<String>();
        //add tasks on ArrayList
        for (int i = 0; i < ArrayDadosTarefas.terraplanagem.length; i++) {
            terraplanagem.add(ArrayDadosTarefas.terraplanagem[i]);
        }
        Collections.sort(terraplanagem);//in order
        for (int i = 0; i < ArrayDadosTarefas.asfaltamento.length; i++) {
            asfaltamento.add(ArrayDadosTarefas.asfaltamento[i]);
        }
        Collections.sort(asfaltamento);
        for (int i = 0; i < ArrayDadosTarefas.condominio.length; i++) {
            condominio.add(ArrayDadosTarefas.condominio[i]);
        }
        Collections.sort(condominio);
        //add task to project
        projetosTreeMap.put("Terraplanagem Aeroporto", terraplanagem);
        projetosTreeMap.put("Asfaltamento Periferia", asfaltamento);
        projetosTreeMap.put("Condominio Centro", condominio);

        return projetosTreeMap;
    }

    public TreeMap<Projeto, List<Tarefa>> getTreeMapBeanProjetosTarefas() {
        return this.projetosTreeMapBean;
    }

    @Deprecated
    /**bring the beans Projeto and Tarefa of Xml file and translate to TreeMap of String
     * @param inverteAgrupamento if True invert the agroupment of TreeMap grouping by tasks containing its project
     *                           if False group by projects containing tasks
     * @return TreeMap String, ListString of personal projects with tasks **/
    public TreeMap<String, List<String>> getTarefas(boolean inverteAgrupamento){
        //TreeMap of personal projects translated from beans to string
        TreeMap<String, List<String>> projetosTreeMapString = new TreeMap<String, List<String>>();
        //for every project containing its tasks...
        for (Map.Entry<Projeto, List<Tarefa>> projetosTarefasBean : this.projetosTreeMapBean.entrySet()){
            Projeto projetoBean = projetosTarefasBean.getKey();//get current project
            List<Tarefa> tarefasBean = projetosTarefasBean.getValue();//get its tasks
            if (inverteAgrupamento) {//if has to reverse the agroupment...
                String nomeProjeto = projetoBean.getNome();
                //...for every task add on TreeMap of String the task and its project, no sublist...
                for (Tarefa tarefa : tarefasBean) {
                    SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyy", new Locale("pt","BR"));
                    String data = formataData.format(tarefa.getVencimento());
                    List<String> projetosString = new ArrayList<String>();
                    projetosString.add(0, nomeProjeto + " [" + data + ']');
                    projetosTreeMapString.put(tarefa.getNome(), projetosString);
                }
            }else{//...add on TreeMap of String the project with task sublist
                List<String> tarefasString = new ArrayList<String>();
                for (Tarefa tarefa : tarefasBean)
                    tarefasString.add(tarefa.getNome());
                projetosTreeMapString.put(projetoBean.getNome(), tarefasString);
            }
        }
        return projetosTreeMapString;
    }

}
