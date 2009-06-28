package condivisi;

import java.io.Serializable;

/**
 * Struttura dati restituita dal server al peer per informarlo
 * delle porta dei tracker
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version bitCreekPeer 1.0
 */
public class Porte implements Serializable {

    /* Costanti */
    /**
     * Costante che definisce la versione della classe
     */
    public static final long serialVersionUID = 17;

    /* Variabili d'istanza */
    /** Porta del tracker TCP */
    private int portaTCP;
    /** Porta del tracker UDP */
    private int portaUDP;
    /** Dice se il file era già stato pubblicato */
    private boolean pubblicato;
    /** Id dello swarm */
    private int id;

    /**
     * Costruttore
     * @param portatcp
     * @param portaudp
     * @param id
     * @throws condivisi.ErrorException
     */
    public Porte(int portatcp, int portaudp, int id) throws ErrorException {
        if (portatcp <= 0 || portaudp <= 0) {
            throw new ErrorException("param null");
        }
        this.portaTCP = portatcp;
        this.portaUDP = portaudp;
        this.pubblicato = false;
        this.id = id;
    }

    /**
     * Restituisce la porta TCP
     * @return portaTCP
     */
    public int getPortaTCP() {
        return this.portaTCP;
    }

    /**
     * Restituisce la porta UDP
     * @return portaUDP
     */
    public int getPortaUDP() {
        return this.portaUDP;
    }

    /**
     * Restituisce il booleano pubblicato che ci dice
     * se un file era già stato pubblicato o no
     * @return pubblicato
     */
    public boolean getPubblicato() {
        return this.pubblicato;
    }

    /**
     * Restituisce l'id dello swarm
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Setta il booleano pubblicato a true
     */
    public void setPubblicato() {
        this.pubblicato = true;
    }
}
