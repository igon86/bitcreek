package peer;

import java.io.Serializable;

/**
 * Classe che definisce i messaggi scambiati tra peer
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Messaggio implements Serializable {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 67;
    /** Codice del messaggio INTERESTED */
    protected static final int INTERESTED = 0;
    /** Codice del messaggio NOT_INTERESTED */
    protected static final int NOT_INTERESTED = 1;
    /** Codice del messaggio CHOCKE */
    protected static final int CHOKE = 2;
    /** Codice del messaggio UNCHOCKE */
    protected static final int UNCHOKE = 3;
    /** Codice del messaggio HAVE */
    protected static final int HAVE = 4;
    /** Codice del messaggio CHUNK */
    protected static final int CHUNK = 5;
    /** Codice del messaggio REQUEST */
    protected static final int REQUEST = 6;
    /** Codice del messaggio CLOSE */
    protected static final int CLOSE = 7;

    /* Variabili d' istanza */
    /** Tipo del messaggio */
    private int tipo;
    /** Corpo del messaggio */
    private Object corpo;

    /**
     * Costruttore
     * @param tipo tipo del msg
     * @param corpo del msg
     */
    public Messaggio(int tipo, Object corpo) {
        this.tipo = tipo;
        this.corpo = corpo;
    }

    /**
     * Restituisce il tipo del messaggio
     * @return tipo
     */
    public int getTipo() {
        return this.tipo;
    }

    /**
     * Restituisce il corpo del messaggio
     * @return corpo del msg
     */
    public Object getObj() {
        return this.corpo;
    }
}
