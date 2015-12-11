package br.com.gpaengenharia.classes.xmls;

import android.content.Context;
import android.util.Log;
import org.xmlpull.v1.XmlSerializer;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.com.gpaengenharia.beans.Usuario;
import br.com.gpaengenharia.classes.WebService;

/**
 * Calls the webservice method that returns the XML file for week tasks
 */
public class XmlTarefasSemana extends Xml implements XmlInterface{

    public XmlTarefasSemana(Context contexto) {
        super(contexto);
        setNomeArquivoXML();
    }

    //name of the file to save XML
    private final static String nomeArquivoXML = "tarefasSemana.xml";

    public static String getNomeArquivoXML() {
        return nomeArquivoXML;
    }

    /** {@inheritDoc} */
    @Override
    public void setNomeArquivoXML() {
        super.nomeArquivoXML = this.nomeArquivoXML;
    }

    /** {@inheritDoc} */
    @Override
    public void setArquivoXML() {
        criaXmlProjetosSemanaTeste();
    }

    /**
     * downloads the XML by webservice and save localy
     * @param usuario
     * @return true: there is update, false: there is no update
     * @throws java.io.IOException
     */
    public static boolean criaXmlProjetosSemanaWebservice(Usuario usuario, boolean forcarAtualizacao) throws IOException {
        /**
         * TODO do not let the webservice be called without restrictions
         */
        WebService webService = new WebService();
        webService.setUsuario(usuario);
        webService.setForcarAtualizacao(forcarAtualizacao);
        String xml = webService.projetosSemana();
        if (xml != null) {
            escreveXML(xml);
            return true;
        }else
            return false;
    }

    /**
     * rewrite the XML passed by parameter, this method is used by
     * 'gravar comentario' method on activity 'AtvTarefa'
     * @param xml
     * @throws IOException
     */
    public void criaXmlProjetosSemanaWebservice(String xml) throws IOException {
        escreveXML(xml);
    }

    /**
    create a xml file and save on directory project
     */
    public void criaXmlProjetosSemanaTeste() {
        try {
            this.arquivoXML = super.contexto.openFileOutput(super.nomeArquivoXML, 0);
        } catch (FileNotFoundException e) {
            Log.e("erro IO", e.getMessage());
        }
        XmlSerializer serializadorXML = android.util.Xml.newSerializer();
        try {
            serializadorXML.setOutput(this.arquivoXML, "UTF-8");
            serializadorXML.startDocument(null, Boolean.valueOf(true));
            serializadorXML.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializadorXML.startTag(null, "GPA");
            serializadorXML.startTag(null, "projetosPessoais-semana");
            for (int projeto=1; projeto<5; projeto++) {
                serializadorXML.startTag(null, "projeto");
                serializadorXML.attribute(null, "nome", "Projeto Semana Exemplo " + String.valueOf(projeto));
                for (int tarefa=1; tarefa<5; tarefa++) {
                    serializadorXML.startTag(null, "tarefa");
                    serializadorXML.text("Tarefa Semana Exemplo " + tarefa + projeto);
                    serializadorXML.endTag(null, "tarefa");;
                }
                serializadorXML.endTag(null, "projeto");
            }
            serializadorXML.endTag(null, "projetosPessoais-semana");
            serializadorXML.endTag(null, "GPA");
            serializadorXML.endDocument();
            serializadorXML.flush();
            this.arquivoXML.close();
        } catch (Exception e) {
            Log.e("erro serializerXML", e.getMessage());
        }
    }

}
