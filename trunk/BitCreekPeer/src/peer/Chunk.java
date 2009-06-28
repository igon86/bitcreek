package peer;

import java.io.Serializable;

/**
 * Classe che rappresenta un chunk di un file
 * @author andrea
 */
public class Chunk implements Serializable{

    /**
     *
     */
    public static final long serialVersionUID = 45;

    private byte[] data;
    // spero non si voglia trasferire gighi di roba!! :::: io una prova la faccio sicuro!!!..se non va vengo
    // a lamentarmi da te
    private int offset;
    private int dim;
    

    /**
     *
     * @param data
     * @param offset
     * @param dim
     */
    public Chunk(byte[] data,int offset,int dim){
        this.data = data;
        this.offset = offset;
        this.dim = dim;
    }

    /**
     *
     * @return
     */
    public int getOffset(){
        return this.offset;
    }

    /**
     *
     * @return
     */
    public int getDim(){
        return this.dim;
    }

    /**
     *
     * @return
     */
    public byte[] getData(){
        return this.data;
    }
}
