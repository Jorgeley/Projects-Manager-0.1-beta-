package br.com.gpaengenharia.classes.xmls;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.gpaengenharia.activities.AtvLogin;
import br.com.gpaengenharia.classes.WebService;

/**
 * Calls the webservice method that returns the users XML
 */
public class XmlUsuario extends Xml implements XmlInterface {
    private static String ultimaSincronizacao;

    public XmlUsuario(Context contexto) {
        super(contexto);
        setNomeArquivoXML();File arquivo = new File(contexto.getFilesDir() + "/" + this.getNomeArquivoXML());
        SimpleDateFormat formatoData = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", new Locale("pt", "BR"));
        Date data = new Date();
        data.setTime(arquivo.lastModified());//get time modification of file
        this.ultimaSincronizacao = formatoData.format(data);
    }

    //name of the file to save XML
    private final static String nomeArquivoXML = "usuarios.xml";

    public static String getNomeArquivoXML() {
        return nomeArquivoXML;
    }

    @Override
    public void setArquivoXML() {

    }

    /** {@inheritDoc} */
    @Override
    public void setNomeArquivoXML() {
        super.nomeArquivoXML = this.nomeArquivoXML;
    }

    /**
      * downloads the XML by webservice and save localy
     * @param usuario
     * @return true: there is update, false: there is no update
     * @throws java.io.IOException
     */
    public boolean criaXmlUsuariosWebservice(boolean forcarAtualizacao) throws IOException {
        /**
         * TODO do not let the webservice be called without restrictions
         */
        WebService webService = new WebService();
        webService.setForcarAtualizacao(forcarAtualizacao);
        webService.setUsuario(AtvLogin.usuario);
        String xml = webService.getUsuarios(this.ultimaSincronizacao);
        if (xml != null) {
            escreveXML(xml);
            return true;
        }else
            return false;
    }

    /**
     * rewrite the XML passed by parameter
     * @param xml
     * @throws java.io.IOException
     */
    public void criaXmlUsuariosWebservice(String xml) throws IOException {
        escreveXML(xml);
    }

}
