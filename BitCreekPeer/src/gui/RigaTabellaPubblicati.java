package gui;

import java.net.InetAddress;

/**
 * Definisce una riga della tabella dei file in upload
 * @author Bandettini
 */
public class RigaTabellaPubblicati {

    /* Costanti */
    /**
     *
     */
    public static final int NONATTIVO = -1;
    private final int K = 1024;

    /* Variabili d'istanza */
    private String file;
    private String dimensione;
    private String stato;
    private String situazione;
    private int peer;
    private int peercercano;
    private InetAddress identita;

    /**
     * Costruttore
     * @param file nome del file
     * @param dimensione dimensione del file
     * @param pubblicato true se è stato pubblicato dal peer, false altrimenti
     */
    public RigaTabellaPubblicati(String file, long dimensione, boolean pubblicato) {
        this.file = file;
        this.dimensione = this.Dim(dimensione);
        this.stato = "In Upload";
        this.situazione = "Non Attivo";
        this.peer = 0;
        this.identita = null;
        if (pubblicato) {
            this.peercercano = 0;
        } else {
            this.peercercano = NONATTIVO;
        }
    }

    /**
     * Restituisce il nome del file
     * @return file
     */
    public String getFile() {
        return this.file;
    }

    /**
     * Restituisce la dimensione del file
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
     * Restituisce la situazione del file
     * @return situazione
     */
    public String getSituazione() {
        return this.situazione;
    }

    /**
     * Restituisce i peer del file
     * @return peer
     */
    public int getPeer() {
        return this.peer;
    }

    /**
     * Restituisce il numero dei peer che hanno cercato il file
     * @return peercercano, -1 se il file non è sttao publicato dal peer
     */
    public int getPeerCerca() {
        return this.peercercano;
    }

    /**
     * restituisce l' ip di chi ha cercato per ultimo il file
     * @return identita, null se il peer non ha pubblicato il file
     */
    public InetAddress getIdentita() {
        return this.identita;
    }

    /**
     * Setta la situazione del file
     * @param situazione
     */
    public void setSituazione(String situazione) {
        this.situazione = situazione;
    }

    /**
     * Setta il numero dei peer
     * @param peer
     */
    public void setPeer(int peer) {
        if (peer < 0) {
            peer = 0;
        }
        this.peer = peer;
    }

    /**
     * Incrementa il numero dei peer che hanno cercato quel file
     * @param np
     */
    public void setPeerCerca(int np) {
        if (peercercano != NONATTIVO && np != NONATTIVO) {
            if (np > this.peercercano) {
                this.peercercano = np;
            }
        }
    }

    /**
     * Setta l'ip dell'ultimo peer che ha cercato quel file
     * @param ind
     */
    public void setIdentita(InetAddress ind) {
        if (ind != null && peercercano != NONATTIVO) {
            this.identita = ind;
        }
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
