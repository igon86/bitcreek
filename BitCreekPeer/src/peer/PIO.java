
package peer;

import java.io.Serializable;

/**
 * Informazioni relative ad un chunk da scaricare
 * @author andrea
 */
public class PIO implements Serializable, Comparable<PIO>{
    
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
    
    /**
     * il compare tiene conto di 2 fattori: il booleano busy e l'int rarita
     * un PIO busy deve essere sicuramente posto dopo un PIO non busy
     * @param arg0
     * @return
     */
    @SuppressWarnings("empty-statement")
    public int compareTo(PIO arg0) {
        int ret;
        if(this.busy && ! arg0.busy){
            ret = 1000+(this.rarita - arg0.rarita);
        }
        else if(! this.busy && arg0.busy){
            ret = -1000+(this.rarita - arg0.rarita);;
        }
        else{
            ret =(this.rarita - arg0.rarita);;
        }
        return ret;
    }
}


