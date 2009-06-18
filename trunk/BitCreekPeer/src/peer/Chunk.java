package peer;

import java.io.Serializable;

/**
 * Classe che rappresenta un chunk di un file
 * @author andrea
 */
public class Chunk implements Serializable{

    public static final long serialVersionUID = 45;

    private byte[] data;
    //spero non si voglia  trasferire gighi di roba!!
    private int offset;
    private int dim;
    

    public Chunk(byte[] data,int offset,int dim){
        this.data=data;
        this.offset=offset;
        this.dim=dim;
    }

    public int getOffset(){
        return this.offset;
    }

    public int getDim(){
        return this.dim;
    }

    public byte[] getData(){
        return this.data;
    }
}
