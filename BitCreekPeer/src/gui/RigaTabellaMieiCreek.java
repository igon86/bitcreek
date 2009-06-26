package gui;

import condivisi.ErrorException;

/**
 * Definisce una riga della tabella dei file in download
 * @author Bandettini
 */
public class RigaTabellaMieiCreek {

    /* Costanti */
    private final int K = 1024;

    /* Variabili d'istanza */
    private String file;
    private String dimensione;
    private String stato;
    private String situazione;
    private String percentuale;
    private String peer;

    /**
     * Costruttore
     * @param file nome del file
     * @param dimensione dimensione del file
     */
    public RigaTabellaMieiCreek(String file, long dimensione, boolean situazione, int percentuale, int peer) {
        this.file = file;
        this.dimensione = this.Dim(dimensione);
        this.stato = "In download";
        if (situazione) {
            this.situazione = "Attivo";
        } else {
            this.situazione = "Non Attivo";
        }
        this.percentuale = percentuale + "%";
        this.peer = "" + peer;
    }

    /**
     * Resituisce il nome del file
     * @return file
     */
    public String getFile() {
        return this.file;
    }

    /**
     * restituisce la dimensione del file
     * @return dimensione
     */
    public String getDimensione() {
        return this.dimensione;
    }

    /**
     * Restituisce lo stato del file
     * @return stato
     */
    public String getStato() {
        return this.stato;
    }

    /**
     * Restituisce la situaizone del file
     * @return situazione
     */
    public String getSituazione() {
        return this.situazione;
    }

    /**
     * Restituisce la percentuale del file
     * @return percentuale
     */
    public String getPercentuale() {
        return this.percentuale;
    }

    /**
     * Restituisce il numero di peer del file
     * @return peer
     */
    public String getPeer() {
        return this.peer;
    }

    /**
     * Setta lo stato del file
     * @param stato
     * @exception condivisi.ErrorException se stato è null
     */
    public void setStato(String stato) throws ErrorException {
        if (stato == null) {
            throw new ErrorException("Param null");
        }
        this.stato = stato;
    }

    /**
     * Setta la situazione del file
     * @param situazione
     * @exception condivisi.ErrorException se situaizone è null
     */
    public void setSituazione(String situazione) throws ErrorException {
        if (situazione == null) {
            throw new ErrorException("Param null");
        }
        this.situazione = situazione;
    }

    /**
     * Setta la percentuale di completamenti del file
     * @param percentuale
     * @exception condivisi.ErrorException se percentuale è null
     */
    public void setPercentuale(String percentuale) throws ErrorException {
        if (percentuale == null) {
            throw new ErrorException("Param null");
        }
        this.percentuale = percentuale;
    }

    /**
     * Setta il numero dei peer del file
     * @param peer
     * @exception condivisi.ErrorException se peer è null
     */
    public void setPeer(String peer) throws ErrorException {
        if (peer == null) {
            throw new ErrorException("Param null");
        }
        this.peer = peer;
    }

    private String Dim(long dimensione){
        int i = 0;
        for (i = 0; ; i++){
            if( (dimensione / K) < 1)
                break;
            else
                dimensione = dimensione / K;
        }
        if(i == 1)return dimensione + " Kbyte";
        if(i == 2)return dimensione + " Mbyte";
        if(i == 3)return dimensione + " Gbyte";
        if(i == 4)return dimensione + " Tbyte";
        return dimensione + " byte";
    }
}
