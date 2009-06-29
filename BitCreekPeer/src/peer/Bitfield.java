package peer;

import java.io.Serializable;

/**
 * Classe che definisce i pezzi che un peer ha.
 * Rappresenta la risposta ad una contact.
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Bitfield implements Serializable {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 45;

    /* Variabili d'istanza */
    /** Array di pezzi che verrà inviato */
    private boolean[] bitfield;

    /**
     * Costruttore
     * @param bitfield array di bitfield
     */
    public Bitfield(boolean[] bitfield) {
        this.bitfield = bitfield;
    }

    /**
     * Restituisce  l' array di bitfield incapsulato
     * in questo oggetto
     * @return bitfield
     */
    public boolean[] getBitfield() {
        return this.bitfield;
    }
}
