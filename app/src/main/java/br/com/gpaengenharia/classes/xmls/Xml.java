package br.com.gpaengenharia.classes.xmls;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import br.com.gpaengenharia.beans.Equipe;
import br.com.gpaengenharia.beans.Projeto;
import br.com.gpaengenharia.beans.Tarefa;
import br.com.gpaengenharia.beans.Usuario;
import br.com.gpaengenharia.classes.WebService;

/**
 * reads xml 'nomeArquivoXML' and save on TreeMap <Projeto, List<Tarefa>>
 * ...containing every project with its task list
 */
public class Xml{
    protected static Context contexto;
    //TreeMap of beans containing every project with its task list
    private TreeMap<Projeto,List<Tarefa>> projetos = new TreeMap<Projeto,List<Tarefa>>();
    protected static String nomeArquivoXML;//name of the file
    //file to save every XML (Personal, Team, etc)
    protected static FileOutputStream arquivoXML;
    private static Set<Integer> idsTarefas = new HashSet<Integer>();
    //name of the file to save the updated tasks
    private static final String nomeArquivoXMLatualizadas = "tarefasAtualizadas.xml";

    public Xml(Context contexto){
        this.contexto = contexto;
    }

    public static String getNomeArquivoXMLatualizadas() {
        return nomeArquivoXMLatualizadas;
    }

    /**
     * save local XML in case of new ones and returns its ID's
     * @param usuario
     * @param ultimaSincronizacao
     * @return matriz matrix of objects with id's of updated tasks and flags of which hast to update
     * @throws IOException
     */
    public static Vector<Vector<Object>> sincronizaXmlTudoWebservice(Usuario usuario, String ultimaSincronizacao) throws IOException {
        /**
         * TODO do not let the webservice to be called without restrictions
         */
        WebService webService = new WebService();
        webService.setUsuario(usuario);
        Object[] respostas = webService.sincroniza(ultimaSincronizacao);
        if (respostas != null) {
            try {
                if (respostas[1] != null) {
                    arquivoXML = contexto.openFileOutput(nomeArquivoXMLatualizadas, 0);
                    arquivoXML.write(respostas[1].toString().getBytes());
                    arquivoXML.close();
                }
            } catch (FileNotFoundException e) {
                Log.e("erro IO", e.getMessage());
            }
            Vector<Vector<Object>> vetorRespostas = new Vector<>();
            vetorRespostas.add( (Vector<Object>) respostas[0] );
            vetorRespostas.add( (Vector<Object>) respostas[2] );
            return vetorRespostas;
        }else
            return null;
    }

    /**
     * Rewrite the XML file, this method is called by children classes...
     * 'XmlTarefasPessoais', 'XmlTarefasEquipes', etc...
     * @param xml
     * @throws IOException
     */
    protected static void escreveXML(String xml) throws IOException {
        try {
            arquivoXML = contexto.openFileOutput(nomeArquivoXML, 0);
            arquivoXML.write(xml.getBytes());
            arquivoXML.close();
        } catch (FileNotFoundException e) {
            Log.e("erro IO", e.getMessage());
        }
    }

    /** open the XML file for reading and returns the TreeMap of beans <Projeto List<Tarefa>>
     * @return TreeMap Projeto(bean), ListTarefa(bean) */
    public TreeMap<Projeto, List<Tarefa>> leXmlProjetosTarefas(){
        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            //InputStream in_s = contexto.getAssets().open(nomeArquivoXML);
            InputStream in_s = this.contexto.openFileInput(this.nomeArquivoXML);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            parseXML(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.projetos;//returns the TreeMap of beans
    }

    /**for every node of Xml, add on TreeMap of beans <Projeto List<Tarefa>>
     * @param parser
     * @throws XmlPullParserException,IOException */
    private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException{
        //beans list of Task node for every project in TreeMap
        List<Tarefa> tarefas = new ArrayList<Tarefa>();
        int tipoEvento = parser.getEventType();
        Projeto projetoAtual = null;
        idsTarefas.clear();
        Set<String> tagsProjeto = new HashSet<String>(Arrays.asList("nome", "equipe", "responsavel"));
        Set<String> tagsTarefa = new HashSet<String>(Arrays.asList("nome", "responsavel", "descricao", "comentarios", "vencimento", "status"));
        //while do not reach the end of file...
        while (tipoEvento != XmlPullParser.END_DOCUMENT){
            String nomeNode = parser.getName();
            switch (tipoEvento){
                case XmlPullParser.START_TAG: //if is a begin of a new tag of the XML...
                    if (nomeNode.equals("projeto")) {//...if tag is project...
                        projetoAtual = new Projeto(Parcel.obtain());
                        projetoAtual.setId(Integer.valueOf(parser.getAttributeValue(0)));
                        parser.nextTag();
                        nomeNode = parser.getName();
                        while (tagsProjeto.contains(nomeNode)) {
                            switch (nomeNode) {
                                case "nome":
                                    projetoAtual.setNome(parser.nextText());
                                    break;
                                case "equipe":
                                    Equipe equipe = new Equipe(Parcel.obtain());
                                    equipe.setId(Integer.valueOf(parser.getAttributeValue(0)));
                                    equipe.setNome(parser.nextText());
                                    projetoAtual.setEquipe(equipe);
                                    break;
                                case "responsavel":
                                    Usuario responsavel = new Usuario(Parcel.obtain());
                                    responsavel.setId(Integer.valueOf(parser.getAttributeValue(0)));
                                    responsavel.setNome(parser.nextText());
                                    projetoAtual.setUsuario(responsavel);
                                    //projetoAtual.setUsuario(parser.nextText());
                                    break;
                            }
                            parser.nextTag();
                            nomeNode = parser.getName();
                        }
                    }
                    if (nomeNode.equals("tarefa")) {//...if tag is task...
                        Tarefa tarefaAtual = new Tarefa(Parcel.obtain());
                        tarefaAtual.setId(Integer.valueOf(parser.getAttributeValue(0)));
                        idsTarefas.add(Integer.valueOf(parser.getAttributeValue(0)));
                        parser.nextTag();
                        nomeNode = parser.getName();
                        while (tagsTarefa.contains(nomeNode)) {
                            switch (nomeNode){
                                case "nome" : tarefaAtual.setNome(parser.nextText()); break;
                                case "responsavel" :
                                    Usuario responsavel = new Usuario(Parcel.obtain());
                                    responsavel.setId(Integer.valueOf(parser.getAttributeValue(0)));
                                    responsavel.setNome(parser.nextText());
                                    tarefaAtual.setUsuario(responsavel);
                                    break;
                                case "descricao" : tarefaAtual.setDescricao(parser.nextText()); break;
                                case "comentarios" :
                                    parser.nextTag();
                                    nomeNode = parser.getName();
                                    tarefaAtual.setComentario("");
                                    while (nomeNode.equals("comentario")) {
                                        tarefaAtual.setComentario(
                                                tarefaAtual.getComentario()
                                                +parser.nextText()+"\n"
                                        );
                                        parser.nextTag();
                                        nomeNode = parser.getName();
                                    }
                                    break;
                                case "vencimento" :
                                    SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yy", new Locale("pt", "BR"));
                                    Date data = null;
                                    try {
                                        data = formatoData.parse(parser.nextText());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    tarefaAtual.setVencimento(data);
                                    break;
                                case "status":
                                    tarefaAtual.setStatus(parser.nextText());
                                    break;
                            }
                            parser.nextTag();
                            nomeNode = parser.getName();
                        }
                        tarefaAtual.setProjeto(projetoAtual);
                        if (!tarefaAtual.getStatus().equals("excluir"))
                            tarefas.add(tarefaAtual);//add bean task on list
                    }
                    break;
                case XmlPullParser.END_TAG://if reach the end of tag...
                    if (nomeNode.equalsIgnoreCase("projeto")) {//...if tag is project...
                        //...add on TreeMap of beans Project and List<Task>
                        this.projetos.put(projetoAtual, tarefas);
                        tarefas = new ArrayList<Tarefa>();
                    }
                    break;
            }
            tipoEvento = parser.next();
        }
        //Log.i("idsTarefas", String.valueOf(idsTarefas));
        //this.log();
    }

    /**
     * returns treeMap of beans Projeto containing beans Tarefa that was updated by webservice
     * @param idsTarefas
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public TreeMap<Projeto, List<Tarefa>> leXmlProjetosTarefas(Vector<Object> idsTarefas) throws XmlPullParserException,IOException {
        XmlPullParserFactory pullParserFactory;
        List<Tarefa> tarefas = null;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            //InputStream in_s = contexto.getAssets().open(nomeArquivoXML);
            InputStream in_s = this.contexto.openFileInput(this.getNomeArquivoXMLatualizadas());
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            //list of beans Tarefa node of every project on TreeMap
            tarefas = new ArrayList<Tarefa>();
            int tipoEvento = parser.getEventType();
            Projeto projetoAtual = null;
            Set<String> tagsProjeto = new HashSet<String>(Arrays.asList("nome", "equipe", "responsavel"));
            Set<String> tagsTarefa = new HashSet<String>(Arrays.asList("nome", "responsavel", "descricao", "comentarios", "vencimento", "status"));
            //while do not reach the end of XML file...
            while (tipoEvento != XmlPullParser.END_DOCUMENT) {
                String nomeNode = parser.getName();
                switch (tipoEvento) {
                    case XmlPullParser.START_TAG: //if is the begin of a new tag...
                        if (nomeNode.equals("projeto")) {//...if tag is project...
                            projetoAtual = new Projeto(Parcel.obtain());
                            projetoAtual.setId(Integer.valueOf(parser.getAttributeValue(0)));
                            parser.nextTag();
                            nomeNode = parser.getName();
                            while (tagsProjeto.contains(nomeNode)) {
                                switch (nomeNode) {
                                    case "nome":
                                        projetoAtual.setNome(parser.nextText());
                                        break;
                                    case "equipe":
                                        Equipe equipe = new Equipe(Parcel.obtain());
                                        equipe.setId(Integer.valueOf(parser.getAttributeValue(0)));
                                        equipe.setNome(parser.nextText());
                                        projetoAtual.setEquipe(equipe);
                                        break;
                                    case "responsavel":
                                        Usuario responsavel = new Usuario(Parcel.obtain());
                                        responsavel.setId(Integer.valueOf(parser.getAttributeValue(0)));
                                        responsavel.setNome(parser.nextText());
                                        projetoAtual.setUsuario(responsavel);
                                        //projetoAtual.setUsuario(parser.nextText());
                                        break;
                                }
                                parser.nextTag();
                                nomeNode = parser.getName();
                            }
                        }
                        if (nomeNode.equals("tarefa") && idsTarefas!=null && idsTarefas.contains(Integer.valueOf(parser.getAttributeValue(0)))) {//...se tag é tarefa...
                            Tarefa tarefaAtual = new Tarefa(Parcel.obtain());
                            tarefaAtual.setId(Integer.valueOf(parser.getAttributeValue(0)));
                            parser.nextTag();
                            nomeNode = parser.getName();
                            while (tagsTarefa.contains(nomeNode)) {
                                switch (nomeNode) {
                                    case "nome":
                                        tarefaAtual.setNome(parser.nextText());
                                        break;
                                    case "responsavel":
                                        Usuario responsavel = new Usuario(Parcel.obtain());
                                        responsavel.setId(Integer.valueOf(parser.getAttributeValue(0)));
                                        responsavel.setNome(parser.nextText());
                                        tarefaAtual.setUsuario(responsavel);
                                        break;
                                    case "descricao":
                                        tarefaAtual.setDescricao(parser.nextText());
                                        break;
                                    case "comentarios":
                                        parser.nextTag();
                                        nomeNode = parser.getName();
                                        tarefaAtual.setComentario("");
                                        while (nomeNode.equals("comentario")) {
                                            tarefaAtual.setComentario(
                                                    tarefaAtual.getComentario()
                                                            + parser.nextText() + "\n"
                                            );
                                            parser.nextTag();
                                            nomeNode = parser.getName();
                                        }
                                        break;
                                    case "vencimento":
                                        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yy", new Locale("pt", "BR"));
                                        Date data = null;
                                        try {
                                            data = formatoData.parse(parser.nextText());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        tarefaAtual.setVencimento(data);
                                        break;
                                    case "status":
                                        tarefaAtual.setStatus(parser.nextText());
                                        break;
                                }
                                parser.nextTag();
                                nomeNode = parser.getName();
                            }
                            tarefaAtual.setProjeto(projetoAtual);
                            tarefas.add(tarefaAtual);//add bean Tarefa in list
                        }
                        break;
                        case XmlPullParser.END_TAG://if reach the end of tag...
                            if (nomeNode.equalsIgnoreCase("projeto")) {//...if tag is project...
                                //...add on TreeMap of beans Projeto and List<Tarefa>
                                this.projetos.put(projetoAtual, tarefas);
                                tarefas = new ArrayList<Tarefa>();
                            }
                        break;
                }
                tipoEvento = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.projetos;
        //this.log();
    }

    /**
     * Return List of beans Team
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public List<Equipe> leXmlEquipes() throws XmlPullParserException,IOException {
        XmlPullParserFactory pullParserFactory;
        List<Equipe> equipes = new ArrayList<>();
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            //InputStream in_s = contexto.getAssets().open(nomeArquivoXML);
            InputStream in_s = contexto.openFileInput(this.nomeArquivoXML);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            int tipoEvento = parser.getEventType();
            //while do not reach the end of file...
            while (tipoEvento != XmlPullParser.END_DOCUMENT) {
                String nomeNode = parser.getName();
                switch (tipoEvento) {
                    case XmlPullParser.START_TAG: //if is the begin of a new tag...
                        if (nomeNode.equals("equipe")) {//...if tag is team...
                            Equipe equipe = new Equipe(Parcel.obtain());
                            equipe.setId(Integer.valueOf(parser.getAttributeValue(0)));
                            parser.nextTag();
                            equipe.setNome(parser.nextText());
                            equipes.add(equipe);
                        }
                        break;
                }
                tipoEvento = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return equipes;
        //this.log();
    }

    /**
     * Returns List of beans Project
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public List<Projeto> leXmlProjetos() throws XmlPullParserException,IOException {
        XmlPullParserFactory pullParserFactory;
        List<Projeto> projetos = new ArrayList<>();
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            //InputStream in_s = contexto.getAssets().open(nomeArquivoXML);
            InputStream in_s = contexto.openFileInput(this.nomeArquivoXML);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            int tipoEvento = parser.getEventType();
            //while do not reach the end of file...
            while (tipoEvento != XmlPullParser.END_DOCUMENT) {
                String nomeNode = parser.getName();
                switch (tipoEvento) {
                    case XmlPullParser.START_TAG: //if is begin of a new tag...
                        if (nomeNode.equals("projeto")) {//...if tag is project...
                            Projeto projeto = new Projeto(Parcel.obtain());
                            projeto.setId(Integer.valueOf(parser.getAttributeValue(0)));
                            parser.nextTag();
                            projeto.setNome(parser.nextText());
                            projetos.add(projeto);
                        }
                        break;
                }
                tipoEvento = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return projetos;
        //this.log();
    }

    /**
     * Returns List of beans Users
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public List<Usuario> leXmlUsuarios() throws XmlPullParserException,IOException {
        XmlPullParserFactory pullParserFactory;
        List<Usuario> usuarios = new ArrayList<>();
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            //InputStream in_s = contexto.getAssets().open(nomeArquivoXML);
            InputStream in_s = contexto.openFileInput(this.nomeArquivoXML);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            int tipoEvento = parser.getEventType();
            //while do not reach the end of file...
            while (tipoEvento != XmlPullParser.END_DOCUMENT) {
                String nomeNode = parser.getName();
                switch (tipoEvento) {
                    case XmlPullParser.START_TAG: //if is the begin of a new tag...
                        if (nomeNode.equals("usuario")) {//...if tag is project...
                            Usuario usuario = new Usuario(Parcel.obtain());
                            usuario.setId(Integer.valueOf(parser.getAttributeValue(0)));
                            parser.nextTag();
                            usuario.setNome(parser.nextText());
                            usuarios.add(usuario);
                        }
                        break;
                }
                tipoEvento = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usuarios;
        //this.log();
    }

    /** generate log of beans Project and Task on TreeMap*/
    public void log(){
        Log.i("qtd",String.valueOf(this.projetos.size()));
        for (Map.Entry<Projeto, List<Tarefa>> projeto : this.projetos.entrySet()){
            String tituloProjeto = projeto.getKey().getNome();
            List<Tarefa> tarefasProjeto = projeto.getValue();
            for (Tarefa tarefa : tarefasProjeto){
                Log.i(projeto.getKey().getId()+':'+tituloProjeto, tarefa.getId()+':'+tarefa.getNome());
            }
        }
    }
}
