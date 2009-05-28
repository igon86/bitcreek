package condivisi;

/**
 * Definisce un nuovo tipo di eccezione
 * @author Bandettini
 */

public class ErrorException extends Exception {

    /* Costanti */

    public static final long serialVersionUID = 28;

    /** Costruttore vuoto */

    public ErrorException(){
        super();
    }

    /**
     * Costruttore
     * @param message
     */

    public ErrorException(String message){
        super(message);
    }
}
