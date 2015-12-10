package br.com.gpaengenharia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import br.com.gpaengenharia.R;
import br.com.gpaengenharia.beans.Equipe;
import br.com.gpaengenharia.beans.Usuario;
import br.com.gpaengenharia.classes.AgendaServico;
import br.com.gpaengenharia.classes.ServicoTarefas;
import br.com.gpaengenharia.classes.Utils;
import br.com.gpaengenharia.classes.WebService;

/**
 * First Activity, Login screen
 */
public class AtvLogin extends Activity{
    public static Usuario usuario; //global object used in entire project
    public static Equipe equipeAdm;
    private AutoCompleteTextView TxtEmail;
    private EditText EdtSenha;
    private LoginTask AtaskLogin = null;
    private String nomeArquivo = "credenciais";
    private File arquivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atv_login);
        Utils.contexto = this;
        this.TxtEmail = (AutoCompleteTextView) findViewById(R.id.email);
        this.EdtSenha = (EditText) findViewById(R.id.password);
        this.PrgLogin = (ProgressBar) findViewById(R.id.PRGlogin);
        this.arquivo = new File(AtvLogin.this.getFilesDir() +"/"+ nomeArquivo);
    }

    @Override
    protected void onResume() {
        if (this.arquivo.exists()){
            try {
                FileInputStream arquivo = this.openFileInput(this.nomeArquivo);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(arquivo));
                //Log.i(buffer.readLine(),buffer.readLine());
                this.AtaskLogin = new LoginTask(buffer.readLine(),buffer.readLine());
                this.AtaskLogin.execute((Void) null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    /**
     * seted directly on OnClick property of BTNlogin
     * @param v
     */
    public void onClickLogin(View v) {
        String login = this.TxtEmail.getText().toString();
        String senha = this.EdtSenha.getText().toString();
        this.AtaskLogin = new LoginTask(login, senha);
        this.AtaskLogin.execute((Void) null);
    }

    /**
     * background login
     */
    private ProgressBar PrgLogin;
    public class LoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String login;

        @Override
        protected void onPreExecute() {
            Utils.barraProgresso(AtvLogin.this, PrgLogin, true);
        }

        private final String senha;

        LoginTask(String login, String senha) {
            this.login = login;
            this.senha = senha;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //TODO not send passwords without security
            usuario = WebService.login(login, senha);//login by webservice
            if (usuario != null) {
                AgendaServico agendaServico = new AgendaServico();
                agendaServico.onReceive(AtvLogin.this, new Intent());
                return true;
            } else
                return false;
        }

        private void gravaArquivo(){
            try {
                FileOutputStream arquivo = AtvLogin.this.openFileOutput(AtvLogin.this.nomeArquivo, 0);
                arquivo.write(this.login.getBytes());
                arquivo.write("\n".getBytes());
                arquivo.write(this.senha.getBytes());
                arquivo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(final Boolean successo) {
            AtaskLogin = null;
            if (successo) {
                if (!AtvLogin.this.arquivo.exists()) {
                    this.gravaArquivo();
                }else{
                    FileInputStream arquivo = null;
                    try {
                        arquivo = AtvLogin.this.openFileInput(AtvLogin.this.nomeArquivo);
                        BufferedReader buffer = new BufferedReader(new InputStreamReader(arquivo));
                        if ( buffer.readLine()!=this.login || buffer.readLine()!=this.senha)
                            this.gravaArquivo();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                /* OUT OF MEMORY!!!
                WebService.tarefas(usuario.getId());*/
                equipeAdm = new Equipe(Parcel.obtain());
                equipeAdm.setId(1);
                if (usuario.getEquipes().contains(equipeAdm)) {
                    usuario.setPerfil("adm");
                    startActivity(new Intent(AtvLogin.this, AtvAdministrador.class));
                }else {
                    usuario.setPerfil("col");
                    startActivity(new Intent(AtvLogin.this, AtvColaborador.class));
                }
                final ServicoTarefas servicoTarefas = new ServicoTarefas();
                servicoTarefas.setContexto(AtvLogin.this);
                servicoTarefas.setNotificacoes(true);//don't create notifications
                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        servicoTarefas.run();
                        return null;
                    }
                }.execute();
                Toast.makeText(AtvLogin.this, "Welcome "+String.valueOf("["+usuario.getPerfil()+"]"+usuario.getNome()), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AtvLogin.this, "invalid user or password", Toast.LENGTH_LONG).show();
            }
            Utils.barraProgresso(AtvLogin.this, PrgLogin, false);
        }
    }

}
