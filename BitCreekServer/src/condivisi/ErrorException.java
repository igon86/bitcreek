package condivisi;

/**
 * Definisce un nuovo tipo di eccezione pensata
 * per BitCreekPeer
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class ErrorException extends Exception {

    /* Costanti */
    /**
     * Costante che definisce la versione della classe
     */
    public static final long serialVersionUID = 28;

    /** Costruttore vuoto */
    public ErrorException() {
        super();
    }

    /**
     * Costruttore
     * @param message stringa con l'errore
     */
    public ErrorException(String message) {
        super(message);
    }
}
