package peer;

import java.io.Serializable;

/**
 * Classe che rappresenta un chunk di un file
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Chunk implements Serializable {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 45;
    /* Varibili d' istanza */
    /** Dati del chunk */
    private byte[] data;
    /** Offset del chunk all' interno del file */
    private int offset;
    /** Dimensione del chunk */
    private int dim;

    /**
     * Costruttore
     * @param data dati del chunk
     * @param offset posizione del chunk nel file
     * @param dim dimensione del chunk
     */
    public Chunk(byte[] data, int offset, int dim) {
        this.data = data;
        this.offset = offset;
        this.dim = dim;
    }

    /**
     * Restituisce l'offset del chunk
     * @return offset
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Restituisce la dimensione del chunk
     * @return dim
     */
    public int getDim() {
        return this.dim;
    }

    /**
     * Restituisce i dati del chunk
     * @return data
     */
    public byte[] getData() {
        return this.data;
    }
}
