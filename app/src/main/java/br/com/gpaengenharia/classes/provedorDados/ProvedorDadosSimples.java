package br.com.gpaengenharia.classes.provedorDados;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProvedorDadosSimples {
    protected List<String> tarefasList;

    //generates the task List
    protected List<String> getDadosList() {

        //create task ArrayList
        List<String> terraplanagem = new ArrayList<String>();
        List<String> asfaltamento = new ArrayList<String>();
        List<String> condominio = new ArrayList<String>();

        //add tasks to ArrayList
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

        return tarefasList;
    }

}
