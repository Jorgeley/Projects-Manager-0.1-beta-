package br.com.gpaengenharia.classes.provedorDados;

/** interface for ProvedorDados's children classes */
public interface ProvedorDadosInterface {

    /**
     * brings the TreeMap of beans Projeto and Tarefa from XML and
     * set on projetosTreeMapBean object
     */
    public abstract void setProjetosTreeMapBean();
}
