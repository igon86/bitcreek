package peer;

import java.io.Serializable;

/**
 * Informazioni relative ad un chunk da scaricare
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class PIO implements Serializable, Comparable<PIO> {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 45;

    /* Variabili d' istanza */
    /** Id del pezzo */
    private int id;
    /** Rarità del pezzo */
    private int rarita;
    /** Flag che indica se il pezzo è occupato */
    private boolean busy;

    /**
     * Costruttore
     * @param id id del PIO
     */
    public PIO(int id) {
        this.id = id;
        this.rarita = 0;
        this.busy = false;
    }

    /**
     * Restituisce l' id del pezzo
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Ci dice se il pezzo è occupato
     * @return busy
     */
    public boolean getBusy() {
        return this.busy;
    }

    /**
     * restituisce la rarità
     * @return rarita
     */
    public int getRarita() {
        return this.rarita;
    }

    /**
     * Setta la rarità del pezzo a rarita
     * @param rarita
     */
    public void setRarita(int rarita) {
        this.rarita = rarita;
    }

    /**
     * Libera il PIO
     */
    public void setFree() {
        this.busy = false;
    }

    /**
     * Occupa il PIO
     */
    public void setBusy() {
        this.busy = true;
    }

    /**
     * Compara il PIO con il PIO passato come parametro
     * @param arg0 PIO da confrontare
     * @return la differenza dei 2 PIO
     */
    public int compareTo(PIO arg0) {
        int ret;
        if (this.busy && !arg0.busy) {
            ret = 1000 + (this.rarita - arg0.rarita);
        } else if (!this.busy && arg0.busy) {
            ret = -1000 + (this.rarita - arg0.rarita);
            ;
        } else {
            ret = (this.rarita - arg0.rarita);
        }
        return ret;
    }
}
