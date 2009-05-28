
package peer;

/**
 * Thread per l'implementazione delle politiche di CHOKE/UNCHOKE delle
 * connessioni in upload di uno swarm
 * @author andrea
 */
public class UploadManager implements Runnable{
    
    BitCreekPeer peer;
    Creek c;
    
    public UploadManager(BitCreekPeer peer, Creek c){
        this.peer = peer;
        this.c= c;
    }

    public void run() {
        System.out.println("UploadManager del creek "+c.getName()+" avviato");
    }

}
