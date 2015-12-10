package br.com.gpaengenharia.classes;

/**
 * Interface of WebServiceAsyncTask class responsible for response of 'onPostExecute'
 */
public interface WebServiceAsyncTaskResposta {
    public void onGetSoapEnvelope(Object envelope);
}
