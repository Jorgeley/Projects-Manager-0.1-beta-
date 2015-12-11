package br.com.gpaengenharia.classes.xmls;

import android.content.Context;
import java.io.IOException;
import br.com.gpaengenharia.classes.WebService;

/**
 * Calls the webservice method that returns the XML for team
 */
public class XmlEquipe extends Xml implements XmlInterface {

    public XmlEquipe(Context contexto) {
        super(contexto);
        setNomeArquivoXML();
    }

    //file name to save
    private final static String nomeArquivoXML = "equipes.xml";

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
     * download the XML by webservice and save localy
     * @return true: there is update, false: there is no update
     * @throws java.io.IOException
     */
    public static boolean criaXmlEquipesWebservice(boolean forcarAtualizacao) throws IOException {
        /**
         * TODO do not let the webservice be called without restrictions
         */
        WebService webService = new WebService();
        webService.setForcarAtualizacao(forcarAtualizacao);
        String xml = webService.getEquipes();
        if (xml != null) {
            escreveXML(xml);
            return true;
        }else
            return false;
    }

    /**
     * Rewrite the XML file passed by parameter
     * @param xml
     * @throws java.io.IOException
     */
    public void criaXmlEquipesWebservice(String xml) throws IOException {
        escreveXML(xml);
    }

}
