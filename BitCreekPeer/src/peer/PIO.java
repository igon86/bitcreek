
package peer;

import java.io.Serializable;

/**
 * Informazioni relative ad un chunk da scaricare
 * @author andrea
 */
public class PIO implements Serializable{
    
    public static final long serialVersionUID = 45;

    private int id;
    private int rarita;
    private boolean busy;
    
    public PIO(int id){
        this.id = id;
        this.rarita = 0;
        this.busy = false;
    }
    
    //getter
    public int getId(){
        return this.id;
    }
    
    public boolean getBusy(){
        return this.busy;
    }
    
    public int getRarita(){
        return this.rarita;
    }
    
    //setter
    public void setRarita(int rarita){
        this.rarita = rarita;
    }
    
    public void setFree(){
        this.busy = false;
    }
    
    public void setBusy(){
        this.busy = true;
    }
}


