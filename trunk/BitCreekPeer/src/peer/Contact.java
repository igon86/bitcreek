
package peer;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Classe che rappresenta il primo messaggio di Handshake inviato tra peer
 * @author andrea
 */
public class Contact implements Serializable{
    
    /**
     *
     */
    public static final long serialVersionUID = 45;
    
    //Informazioni del peer
    
    private int ss;
    private InetAddress ip;
    //id dello swarm a cui sono interessato
    private int id;
    
    /**
     *
     * @param ip
     * @param ss
     * @param id
     */
    public Contact(InetAddress ip,int ss,int id){
       this.id = id;
       this.ip = ip;
       this.ss = ss;
   }
   
   //GETTER
   
    /**
     *
     * @return
     */
    public InetAddress getIp(){
       return this.ip;
   }
   
   /**
    *
    * @return
    */
   public int getId(){
       return this.id;
   }
   
   /**
    *
    * @return
    */
   public int getSS(){
       return this.ss;
   }
}
