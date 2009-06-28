
package peer;

import java.io.Serializable;

/**
 * Messaggio di risposta ad una contact
 * @author andrea
 */
public class Bitfield implements Serializable{
    
    /**
     *
     */
    public static final long serialVersionUID = 45;

    
    private boolean[] bitfield;
        
    /**
     *
     * @param bitfield
     */
    public Bitfield(boolean[] bitfield){
        this.bitfield = bitfield;
    }
    
    /**
     *
     * @return
     */
    public boolean[] getBitfield(){
        return this.bitfield;
    }
}
