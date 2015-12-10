package br.com.gpaengenharia.classes.provedorDados;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import br.com.gpaengenharia.activities.AtvLogin;
import br.com.gpaengenharia.classes.xmls.XmlTarefasArquivadas;
import br.com.gpaengenharia.classes.xmls.XmlTarefasSemana;

/**
 build TreeMap of beans <Projeto, List<Tarefa>>
 inherit from ProvedorDados and implements ProvedorDadosInterface
  */
public class ProvedorDadosTarefasArquivadas extends ProvedorDados implements ProvedorDadosInterface{
    private Context contexto;

    public ProvedorDadosTarefasArquivadas(Context contexto, boolean forcarAtualizacao) {
        this.contexto = contexto;
        File arquivo = new File(contexto.getFilesDir()+"/"+ XmlTarefasArquivadas.getNomeArquivoXML());
        if ( !arquivo.exists() || forcarAtualizacao )
            try {
                XmlTarefasArquivadas xmlTarefasArquivadas = new XmlTarefasArquivadas(this.contexto);
                xmlTarefasArquivadas.criaXmlTarefasArquivadasWebservice(AtvLogin.usuario, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        setProjetosTreeMapBean();
    }

    /** {@inheritDoc} */
    @Override
    public TreeMap<String, List<String>> getTarefas(boolean inverteAgrupamento) {
        return super.getTarefas(inverteAgrupamento);
    }

    /** {@inheritDoc} */
    @Override
    public void setProjetosTreeMapBean() {
        XmlTarefasArquivadas xml = new XmlTarefasArquivadas(this.contexto);
        super.projetosTreeMapBean = xml.leXmlProjetosTarefas();
    }

}
