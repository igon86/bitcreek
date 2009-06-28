
package peer;

import java.io.Serializable;

/**
 *
 * @author andrea
 */
public class Messaggio implements Serializable{
    
    /**
     *
     */
    public static final long serialVersionUID = 67;
    
    /**codici dei messaggi*/
    protected static final int INTERESTED = 0;
    /**
     *
     */
    protected static final int NOT_INTERESTED = 1;
    /**
     *
     */
    protected static final int CHOKE = 2;
    /**
     *
     */
    protected static final int UNCHOKE = 3;
    /**
     *
     */
    protected static final int HAVE = 4;
    /**
     *
     */
    protected static final int CHUNK = 5;
    /**
     *
     */
    protected static final int REQUEST = 6;
    /**
     *
     */
    protected static final int CLOSE = 7;

    private int tipo;
    private Object corpo;
    
    
    /**
     *
     * @param tipo
     * @param corpo
     */
    public Messaggio(int tipo, Object corpo){
        this.tipo = tipo;
        this.corpo = corpo;
    }
    
    //getter
    /**
     *
     * @return
     */
    public int getTipo(){
        return this.tipo;
    }
    
    /**
     *
     * @return
     */
    public Object getObj(){
        return this.corpo;
    }
    
    //non c'e` alcun bisogno dei setter
    
}
