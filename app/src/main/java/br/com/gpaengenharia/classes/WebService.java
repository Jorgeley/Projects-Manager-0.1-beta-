package br.com.gpaengenharia.classes;

import android.os.Parcel;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import br.com.gpaengenharia.activities.AtvLogin;
import br.com.gpaengenharia.beans.Equipe;
import br.com.gpaengenharia.beans.Projeto;
import br.com.gpaengenharia.beans.Tarefa;
import br.com.gpaengenharia.beans.Usuario;

/**
 * Class responsible for comunication between app and server
 */
public class WebService{

    //private static String SERVIDOR = "192.168.0.118:8888";
    //private static String SERVIDOR = "192.168.1.103:8888";
    private static String SERVIDOR = "www.grupo-gpa.com";
    //Namespace of the Webservice - can be found in WSDL
    //private static String NAMESPACE = "http://"+SERVIDOR+"/WEB/GPA/public/webservice/soap/";
    private static String NAMESPACE = "http://"+SERVIDOR+"/webservice/soap/";
    //Webservice URL - WSDL File location
    //private static String URL = "http://"+SERVIDOR+"/WEB/GPA/public/webservice/soap";//Make sure you changed IP address
    private static String URL = "http://"+SERVIDOR+"/webservice/soap";
    //SOAP Action URI again Namespace + Web method name
    //private static String SOAP_ACTION = "http://"+SERVIDOR+"/WEB/GPA/public/webservice/soap#";
    private static String SOAP_ACTION = "http://"+SERVIDOR+"/webservice/soap#";
    //user for webservice operations
    private Usuario usuario;
    //flag sent to server, indicates to return the projects, even already updated
    private boolean forcarAtualizacao = false;

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public boolean isForcarAtualizacao() {
        return this.forcarAtualizacao;
    }

    public void setForcarAtualizacao(boolean forcarAtualizacao) {
        this.forcarAtualizacao = forcarAtualizacao;
    }

    /**
     * login by webservice, returns the Usuario object
     * @param login
     * @param senha
     * @return Usuario
     */
    public static Usuario login(String login, String senha) {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "autentica");
        //set the parameters of webservice method 'autentica'
        PropertyInfo loginWebservice = new PropertyInfo();
        PropertyInfo senhaWebservice = new PropertyInfo();
        loginWebservice.setName("login");
        loginWebservice.setValue(login);
        loginWebservice.setType(String.class);
        requisicao.addProperty(loginWebservice);
        senhaWebservice.setName("senha");
        senhaWebservice.setValue(senha);
        senhaWebservice.setType(String.class);
        requisicao.addProperty(senhaWebservice);
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        //Usuario bean
        Usuario usuario = new Usuario(Parcel.obtain());
        try {//calls 'autentica' webservice method
            androidHttpTransport.call(SOAP_ACTION + "autentica", envelope);
            //getting the response
            Vector<Vector<Vector<Object>>> resposta = (Vector<Vector<Vector<Object>>>) envelope.getResponse();
            usuario.setId((Integer) resposta.get(0).get(0).get(0));
            usuario.setNome((String) resposta.get(0).get(0).get(1));
            usuario.setEquipes(new HashSet<Equipe>());
            Vector<Vector<Object>> equipesObjeto = resposta.get(1);
            for (Vector<Object> equipeObjeto : equipesObjeto) {
                Equipe equipe = new Equipe(Parcel.obtain());
                equipe.setId((Integer) equipeObjeto.get(0));
                equipe.setNome((String) equipeObjeto.get(1));
                usuario.getEquipes().add(equipe);
            }
        } catch (Exception e) {
            //if cannot authenticate, returns null
            usuario = null;
            e.printStackTrace();
        }
        return usuario;
    }

    /**
     * sync the user tasks
     * @param ultimaSincronizacao
     * @return array ids of updated tasks, XML tasks updated and flags of which XML's have to update
     */
    public Object[] sincroniza(String ultimaSincronizacao) {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "sincroniza");
        //setting the parameters of 'sincroniza' webservice method
        requisicao.addProperty(this.getPropertyInfoUsuario());
        PropertyInfo ultimaSincronizacaoWebservice = new PropertyInfo();
        ultimaSincronizacaoWebservice.setName("ultimaSincronizacao");
        ultimaSincronizacaoWebservice.setValue(ultimaSincronizacao);
        ultimaSincronizacaoWebservice.setType(String.class);
        requisicao.addProperty(ultimaSincronizacaoWebservice);
        //Log.i("ultimaSincronizacao", ultimaSincronizacao);
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        Vector<Integer> idsTarefas = null;
        Vector<Boolean> flagsSincroniza = null;
        String xml = null;
        try {//calls the 'sincroniza' webservice method
            androidHttpTransport.call(SOAP_ACTION + "sincroniza", envelope);
            //getting the response
            Vector<Object> resposta = (Vector<Object>) envelope.getResponse();
            if (resposta == null)
                return null;
            else {
                idsTarefas = (Vector<Integer>) resposta.get(0); //id's of updated tasks
                flagsSincroniza = (Vector<Boolean>) resposta.get(2); //flags of which XML's have to update
                xml = resposta.get(1).toString(); //XML containing all updated tasks
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object[] respostas = new Object[]{idsTarefas, xml, flagsSincroniza};
        return respostas;
    }

    /**
     * Conclude (administrator) or request conclusion (colaborator) of task
     * @param tarefa
     * @param confirma
     * @return 'concluida' or 'concluir'
     */
    public String concluiTarefa(Tarefa tarefa, String confirma) {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "concluiTarefa");
        //setting the parameters of 'projetosPessoais' webservice method
        requisicao.addProperty(this.getPropertyInfoUsuario());
        //setting the parameter 'idTarefa' of 'concluiTarefa' webservice method
        PropertyInfo idTarefaWebservice = new PropertyInfo();
        idTarefaWebservice.setName("idTarefa");
        idTarefaWebservice.setValue(tarefa.getId());
        idTarefaWebservice.setType(Integer.class);
        requisicao.addProperty(idTarefaWebservice);
        //setting parameter 'confirma' of 'concluiTarefa' webservice method
        PropertyInfo confirmaWebservice = new PropertyInfo();
        confirmaWebservice.setName("confirma");
        confirmaWebservice.setValue(confirma);
        confirmaWebservice.setType(String.class);
        requisicao.addProperty(confirmaWebservice);
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String resposta = null;
        try {//calls 'projetosPessoais' webservice method
            androidHttpTransport.call(SOAP_ACTION + "concluiTarefa", envelope);
            //getting the response
            resposta = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resposta;
    }

    /**
     * get the XML 'projetosPessoais' with user tasks
     * @return XML
     */
    public String projetosPessoais() {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "projetosPessoais");
        //setting parameters of 'projetosPessoais' webservice method
        requisicao.addProperty(this.getPropertyInfoUsuario());
        requisicao.addProperty(this.getForcarAtualizacao());
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String xml = null;
        try {//calls the 'projetosPessoais' webservice method
            androidHttpTransport.call(SOAP_ACTION + "projetosPessoais", envelope);
            //getting the response
            xml = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    /**
     * get the XML projetosEquipes with tasks of the user's team
     * @return XML
     */
    public String projetosEquipes() {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "projetosEquipes");
        //setting the parameters of 'projetosEquipes' webservice method
        requisicao.addProperty(this.getPropertyInfoUsuario());
        requisicao.addProperty(this.getForcarAtualizacao());
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String xml = null;
        try {//calls 'projetosEquipes' webservice method
            androidHttpTransport.call(SOAP_ACTION + "projetosEquipes", envelope);
            //getting the response
            xml = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    /**
     * get the XML today user's projects
     * @return XML
     */
    public String projetosHoje() {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "projetosHoje");
        //setting the parameters of 'projetosHoje' webservice method
        requisicao.addProperty(this.getPropertyInfoUsuario());
        requisicao.addProperty(this.getForcarAtualizacao());
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String xml = null;
        try {//calls 'projetosHoje' webservice method
            androidHttpTransport.call(SOAP_ACTION + "projetosHoje", envelope);
            //getting the response
            xml = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    /**
     * get the XML week user's projects
     * @return XML
     */
    public String projetosSemana() {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "projetosSemana");
        //setting the parameters of 'projetosSemana' webservice method
        requisicao.addProperty(this.getPropertyInfoUsuario());
        requisicao.addProperty(this.getForcarAtualizacao());
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String xml = null;
        try {//calls 'projetosSemana' webservice method
            androidHttpTransport.call(SOAP_ACTION + "projetosSemana", envelope);
            //getting the response
            xml = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    /**
     * bring the filed tasks (concludeds)
     * @return
     */
    public String tarefasArquivadas(){
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "tarefasArquivadas");
        //setting parameters of 'tarefasArquivadas' webservice method
        requisicao.addProperty(this.getPropertyInfoUsuario());
        requisicao.addProperty(this.getForcarAtualizacao());
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String xml = null;
        try {//calls 'tarefasArquivadas' webservice method
            androidHttpTransport.call(SOAP_ACTION + "tarefasArquivadas", envelope);
            //getting the response
            xml = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    /**
     * save the comment of a user task
     * @param idTarefa
     * @param textoComentario
     * @return String saved comment and flags of whiches XML have to update
     */
    public Object[] gravacomentario(int idTarefa, String textoComentario) {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "gravaComentario");
        //setting parameter 'usuario' of 'gravacomentario' webservice method
        requisicao.addProperty(this.getPropertyInfoUsuario());
        //setting parameter 'idTarefa' of 'gravacomentario' webservice method
        PropertyInfo idTarefaWebservice = new PropertyInfo();
        idTarefaWebservice.setName("idTarefa");
        idTarefaWebservice.setValue(idTarefa);
        idTarefaWebservice.setType(Integer.class);
        requisicao.addProperty(idTarefaWebservice);
        //setting parameter 'textoComentario' of 'gravacomentario' webservice method
        PropertyInfo textoComentarioWebservice = new PropertyInfo();
        textoComentarioWebservice.setName("textoComentario");
        textoComentarioWebservice.setValue(textoComentario);
        textoComentarioWebservice.setType(String.class);
        requisicao.addProperty(textoComentarioWebservice);
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String comentario = null;
        Vector<Boolean> flagsSincroniza = null;
        try {//calls 'gravacomentario' webservice method
            androidHttpTransport.call(SOAP_ACTION + "gravaComentario", envelope);
            //getting the response
            Vector<Object> resposta = (Vector<Object>) envelope.getResponse();
            comentario = (String) resposta.get(0);
            flagsSincroniza = (Vector<Boolean>) resposta.get(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object[] respostas = new Object[]{comentario, flagsSincroniza};
        return respostas;
    }

    /**
     * save project
     * @param projeto
     * @return boolean response
     */
    public static boolean gravaProjeto(Projeto projeto){
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "gravaProjeto");
        PropertyInfo nomeProjeto = new PropertyInfo();
        nomeProjeto.setName("nomeProjeto");
        nomeProjeto.setValue(projeto.getNome());
        nomeProjeto.setType(String.class);
        requisicao.addProperty(nomeProjeto);
        PropertyInfo descricaoProjeto = new PropertyInfo();
        descricaoProjeto.setName("descricaoProjeto");
        descricaoProjeto.setValue(projeto.getDescricao());
        descricaoProjeto.setType(String.class);
        requisicao.addProperty(descricaoProjeto);
        PropertyInfo vencimentoProjeto = new PropertyInfo();
        vencimentoProjeto.setName("vencimentoProjeto");
        vencimentoProjeto.setValue(projeto.getVencimento().toString());
        vencimentoProjeto.setType(String.class);
        requisicao.addProperty(vencimentoProjeto);
        PropertyInfo equipeProjeto = new PropertyInfo();
        equipeProjeto.setName("equipeProjeto");
        if (projeto.getEquipe() != null)
            equipeProjeto.setValue(projeto.getEquipe().getId());
        equipeProjeto.setType(Integer.class);
        requisicao.addProperty(equipeProjeto);
        PropertyInfo usuarioProjeto = new PropertyInfo();
        usuarioProjeto.setName("usuario");
        if (projeto.getUsuario() != null)
            usuarioProjeto.setValue(projeto.getUsuario().getId());
        usuarioProjeto.setType(Integer.class);
        requisicao.addProperty(usuarioProjeto);
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        boolean resposta = false;
        try {//calls 'gravaprojeto' webservice method
            androidHttpTransport.call(SOAP_ACTION + "gravaProjeto", envelope);
            //getting the response
            resposta = (boolean) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resposta;
    }

    /**
     * Save the task
     * @param tarefa
     * @return boolean response
     */
    public static boolean gravaTarefa(Tarefa tarefa){
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "gravaTarefa");
        PropertyInfo id = new PropertyInfo();
        id.setName("id");
        id.setValue(tarefa.getId());
        id.setType(Integer.class);
        requisicao.addProperty(id);
        PropertyInfo nome = new PropertyInfo();
        nome.setName("nome");
        nome.setValue(tarefa.getNome());
        nome.setType(String.class);
        requisicao.addProperty(nome);
        PropertyInfo descricao = new PropertyInfo();
        descricao.setName("descricao");
        descricao.setValue(tarefa.getDescricao());
        descricao.setType(String.class);
        requisicao.addProperty(descricao);
        PropertyInfo vencimento = new PropertyInfo();
        vencimento.setName("vencimento");
        vencimento.setValue(tarefa.getVencimento().toString());
        vencimento.setType(String.class);
        requisicao.addProperty(vencimento);
        PropertyInfo projeto = new PropertyInfo();
        projeto.setName("projeto");
        projeto.setValue(tarefa.getProjeto().getId());
        projeto.setType(Integer.class);
        requisicao.addProperty(projeto);
        PropertyInfo responsavel = new PropertyInfo();
        responsavel.setName("responsavel");
        responsavel.setValue(tarefa.getUsuario().getId());
        responsavel.setType(Integer.class);
        requisicao.addProperty(responsavel);
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        boolean resposta = false;
        try {//calls 'gravatarefa' webservice method
            androidHttpTransport.call(SOAP_ACTION + "gravaTarefa", envelope);
            //getting the response
            resposta = (boolean) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resposta;
    }

    public Vector<Boolean> excluiTarefa(Tarefa tarefa){
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "excluiTarefa");
        PropertyInfo idTarefa = new PropertyInfo();
        idTarefa.setName("idTarefa");
        idTarefa.setValue(tarefa.getId());
        idTarefa.setType(Integer.class);
        requisicao.addProperty(idTarefa);
        requisicao.addProperty(getPropertyInfoUsuario());
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        Vector<Boolean> resposta = null;
        try {//calls 'gravatarefa' webservice method
            androidHttpTransport.call(SOAP_ACTION + "excluiTarefa", envelope);
            //getting the response
            resposta = (Vector<Boolean>) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resposta;
    }

    /**
     * returns team list, used by activity atvProjeto
     * @return XML
     */
    public static String getEquipes() {
        List<Equipe> equipes = null;
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "getEquipes");
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String resposta = null;
        try {//calls 'getEquipes' webservice method
            androidHttpTransport.call(SOAP_ACTION + "getEquipes", envelope);
            //getting the response
            resposta = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resposta;
    }

    /**
     * returns project list, used by activity atvTarefa
     * @return XML
     * @param ultimaSincronizacao
     */
    public String getProjetos(String ultimaSincronizacao) {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "getProjetos");
        requisicao.addProperty(this.getPropertyInfoUsuario());
        PropertyInfo ultimaSincronizacaoWebservice = new PropertyInfo();
        ultimaSincronizacaoWebservice.setName("ultimaSincronizacao");
        ultimaSincronizacaoWebservice.setValue(ultimaSincronizacao);
        ultimaSincronizacaoWebservice.setType(String.class);
        requisicao.addProperty(ultimaSincronizacaoWebservice);
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String resposta = null;
        try {//calls 'getProjetos' webservice method
            androidHttpTransport.call(SOAP_ACTION + "getProjetos", envelope);
            //getting the response
            resposta = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resposta;
    }

    /**
     * returns user list, used by activity atvTarefa
     * @return XML
     * @param ultimaSincronizacao
     */
    public static String getUsuarios(String ultimaSincronizacao) {
        //SOAP request
        SoapObject requisicao = new SoapObject(NAMESPACE, "getUsuarios");
        PropertyInfo ultimaSincronizacaoWebservice = new PropertyInfo();
        ultimaSincronizacaoWebservice.setName("ultimaSincronizacao");
        ultimaSincronizacaoWebservice.setValue(ultimaSincronizacao);
        ultimaSincronizacaoWebservice.setType(String.class);
        requisicao.addProperty(ultimaSincronizacaoWebservice);
        //enveloping the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(requisicao);
        //HTTP request
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String resposta = null;
        try {//calls 'getUsuarios' webservice method
            androidHttpTransport.call(SOAP_ACTION + "getUsuarios", envelope);
            //getting the response
            resposta = (String) envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resposta;
    }

        /**
     * returns the parameter 'usuario' to set on webservice request
     * @return PropertyInfo
     */
    private PropertyInfo getPropertyInfoUsuario(){
        PropertyInfo idUsuarioWebservice = new PropertyInfo();
        idUsuarioWebservice.setName("usuario");
        idUsuarioWebservice.setValue(this.getUsuario().getId());
        idUsuarioWebservice.setType(Integer.class);
        return idUsuarioWebservice;
    }

    /**
     * returns the parameter 'forcarAtualizacao' to set on webservice request
     * @return PropertyInfo
     */
    private PropertyInfo getForcarAtualizacao(){
        PropertyInfo forcarAtualizacao = new PropertyInfo();
        /**
         * on server, the 'novasTarefas' property of 'AclUsuario' class indicates if there is
         * new tasks to be sent, so the 'forcarAtualizacao' webservice method perhaps do not load the tasks
         * because of this property, so the flag below 'forcarAtualizacao' indicates that the server
         * must return the tasks ignoring the 'novasTarefas' property (COP: Cat Oriented Programming)
         */
        forcarAtualizacao.setName("forcarAtualizacao");
        forcarAtualizacao.setValue(this.isForcarAtualizacao());
        forcarAtualizacao.setType(Boolean.class);
        return forcarAtualizacao;
    }

}
